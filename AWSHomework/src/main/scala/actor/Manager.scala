package actor


import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import model._
import java.io._
import java.nio.file.Paths

import scala.io.Source
import akka.japi.Option.Some
import com.amazonaws.auth.{AWSStaticCredentialsProvider, BasicAWSCredentials}
import com.amazonaws.auth.profile.ProfileCredentialsProvider
import com.amazonaws.regions.Regions
import com.amazonaws.services.s3.{AmazonS3, AmazonS3ClientBuilder}
import com.amazonaws.services.s3.model._

import collection.JavaConverters._
import scala.util.{Failure, Success, Try}


object Manager {

  case class GetRequest(fileName: String)

  case class PostRequest(fileName: String)

  case object PostOutFiles

  case object GetInFiles

  def props(client: AmazonS3, bucket: String) = Props(new Manager(client, bucket))
}


class Manager(client: AmazonS3, bucket:String) extends Actor with ActorLogging {

  import Manager._

  val folderPath = "./src/main/resources/s3"
  val inPath = "./src/main/resources/in"
  val outPath = "./src/main/resources/out"
  var recursivePath = ""

  def downloadFile(client: AmazonS3, bucket: String, objectKey: String, fullPath: String): ObjectMetadata = {
    val arr = fullPath.split("/") // split path
    val wanted_file = arr(arr.length - 1)
    val file_directories = new File(fullPath.substring(0, fullPath.length - wanted_file.length)).mkdirs()
    val file = new File(fullPath)
    client.getObject(new GetObjectRequest(bucket, objectKey), file)
  }

  def uploadFile(client: AmazonS3, bucket: String, objectKey: String, filePath: String): PutObjectResult = {
    val metadata = new ObjectMetadata()
    metadata.setContentType("plain/text")
    metadata.addUserMetadata("user-type", "customer")

    val request = new PutObjectRequest(bucket, objectKey, new File(filePath))
    request.setMetadata(metadata)
    client.putObject(request)
  }

  // Resource is https://www.geeksforgeeks.org/java-program-list-files-directory-nested-sub-directories-recursive-approach/
  def makeOutOperation(files: Array[File],index: Int, level: Int): Unit = {

    if(index == files.length) {
      return
    }

    if(files(index).isFile) {
//      println(files(index).getPath.substring(outPath.length + 1))
      uploadFile(client, bucket, files(index).getPath.substring(outPath.length + 1), files(index).getPath)
    }

    else if(files(index).isDirectory) {
      makeOutOperation(files(index).listFiles(), 0,  level + 1)
    }
    makeOutOperation(files,index + 1, level)
  }

  def receive: Receive = {

    case GetRequest(fileName) =>
      val realSender = sender()
      val objectKey = fileName

      if(client.doesObjectExist(bucket, objectKey)) {
        val fullPath = s"${folderPath}/${fileName}"
        downloadFile(client, bucket, objectKey, fullPath)

        realSender ! Right(SuccessfulResponse(200, s"File with filename: ${fileName} has been successfully downloaded."))
        log.info(s"GetRequest successfully finished and downloaded file: ${fileName}, with status 200.")
      } else {
        realSender ! Left(ErrorResponse(404, s"File with filename: ${fileName} is not found in GetRequest request."))
        log.error(s"GetRequest failed to download file: ${fileName} with status 404.")
      }

    case PostRequest(fileName) =>
      val realSender = sender()
      val objectKey = fileName
      if(client.doesObjectExist(bucket, objectKey)) {
        realSender ! Left(ErrorResponse(409, s"File: ${fileName} is already in bucket: ${bucket}."))
        log.info(s"UploadRequest failed to upload file: ${fileName}. It already exists.")
      } else {
        val fullPath = s"${folderPath}/${fileName}"

        Try(uploadFile(client, bucket, objectKey, fullPath)) match {
          case Success(value) =>
            realSender ! Right(SuccessfulResponse(201, s"File with filename: ${fileName} has been successfully uploaded."))
            log.info(s"UploadRequest successfully finished and uploaded file: ${fileName}, with status 201.")

          case Failure(exception) =>
            realSender ! Left(ErrorResponse(500, "Internal server occurred."))
            log.info(s"UploadRequest failed to upload file: ${fileName} with status 500.")
        }
      }

    case PostOutFiles =>
      val outDirectory: File = new File(outPath)
      val filesFromOutDirectory: Array[File] = outDirectory.listFiles()
      makeOutOperation(filesFromOutDirectory, 0, 0)

    case GetInFiles =>
      val objects = client.listObjects(new ListObjectsRequest().withBucketName(bucket))
      objects.getObjectSummaries.forEach(os => downloadFile(client, bucket, os.getKey, inPath + "/" + os.getKey))
  }
}
