package actor

import java.io.{File, InputStream}

import akka.actor.{Actor, ActorLogging, Props}
import com.amazonaws.services.s3.AmazonS3
import com.amazonaws.services.s3.model.{GetObjectRequest, ObjectMetadata, PutObjectRequest}
import model.{SuccessfulResponse, ErrorResponse}


object PhotoActor {
  case class UploadPhoto(inputStream: InputStream, fileName: String, contentType: String)

  case class DownloadPhoto(fileName: String)

  def props(client: AmazonS3, bucketName: String) = Props(new PhotoActor(client, bucketName))
}


class PhotoActor(client: AmazonS3, bucketName: String) extends Actor with ActorLogging {
  import PhotoActor._

  override def receive: Receive = {

    case UploadPhoto(inputStream, fileName, contentType) =>
      // Upload a file as a new object with ContentType and title specified.

      val key = s"photos/$fileName"

      if(client.doesObjectExist(bucketName, key)) {
        sender() ! Left(ErrorResponse(409, s"Photo: ${fileName} is already in the bucket: ${bucketName}."))
        log.error(s"UploadPhoto could not upload photo: ${fileName}. It is already in the bucket.")
      } else {
        val metadata = new ObjectMetadata()
        metadata.setContentType(contentType)
        val request = new PutObjectRequest(bucketName, key, inputStream, metadata)
        val result = client.putObject(request)
        sender() ! Right(SuccessfulResponse(201, s"file version: ${result.getVersionId}"))
        log.info("Successfully put photo with filename: {} to AWS S3", fileName)
      }

      context.stop(self)

    case DownloadPhoto(fileName) =>


  }
}
