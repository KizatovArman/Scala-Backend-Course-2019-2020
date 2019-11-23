import java.io.{BufferedReader, File, InputStreamReader}

import akka.pattern.ask
import akka.http.scaladsl.model._
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import actor.Manager
import akka.http.scaladsl.Http
import akka.actor.ActorSystem
import akka.stream.{ActorMaterializer, Materializer}
import akka.util.Timeout
import com.amazonaws.auth.{AWSStaticCredentialsProvider, BasicAWSCredentials}
import com.amazonaws.auth.profile.ProfileCredentialsProvider
import com.amazonaws.regions.Regions
import com.amazonaws.services.s3.{AmazonS3, AmazonS3ClientBuilder}
import com.amazonaws.services.s3.model.{GetObjectRequest, ListObjectsV2Request, ListObjectsV2Result, ObjectMetadata, PutObjectRequest}
import com.sun.corba.se.spi.ior.ObjectKey

import collection.JavaConverters._
import model._
import org.slf4j.LoggerFactory

import scala.concurrent.ExecutionContextExecutor
import scala.concurrent.duration._


object Boot extends App with SprayJsonSerializer {


  implicit val system: ActorSystem = ActorSystem("task1-service")
  implicit val materializer: Materializer = ActorMaterializer()
  implicit val executionContext: ExecutionContextExecutor = system.dispatcher

  implicit val timeout: Timeout = Timeout(10.seconds)

  val log = LoggerFactory.getLogger("Boot")

  val clientRegion = Regions.AP_SOUTHEAST_1

  val credentials = new BasicAWSCredentials("", "")

  val client = AmazonS3ClientBuilder.standard()
    .withCredentials(new AWSStaticCredentialsProvider(credentials))
    .withRegion(clientRegion)
    .build()


  val bucketName = "newtask1"

  val manager = system.actorOf(Manager.props(client, bucketName), "manager")

//  createBucket(client, bucketName)


  val route =
    path("healthcheck") {
      get {
        complete {
          "ok"
        }
      }
    } ~
    path("file") {
      concat(
        get {
          parameters('filename.as[String]) { fileName =>
            complete {
              (manager ? Manager.GetRequest(fileName)).mapTo[Either[ErrorResponse, SuccessfulResponse]]
            }
          }
        },
        post {
          entity(as[Path]) { up =>
            complete {
              (manager ? Manager.PostRequest(up.path)).mapTo[Either[ErrorResponse, SuccessfulResponse]]
            }
          }
        }
      )
    } ~
  pathPrefix("task") {
    concat(
      path("in") {
        get {
          complete {
            manager ! Manager.GetInFiles
            "Downloaded from IN Directory"
          }
        }
      },
      path("out") {
        get {
          complete {
            manager ! Manager.PostOutFiles
            "Uploaded from OUT Directory"
          }
        }
      }
    )
  }

  val bindingFuture = Http().bindAndHandle(route, "0.0.0.0", 8080)


  def createBucket(s3client: AmazonS3, bucket: String) = {
    if(!s3client.doesBucketExistV2(bucket)) {
      s3client.createBucket(bucket)
      log.info(s"Bucket with name: $bucket created.")
    } else {
      log.info(s"Bucket $bucket already exists.")
      s3client.listBuckets().asScala.foreach(b => log.info(s"Bucket: ${b.getName}"))
    }
  }


//    def getObject(objectKey: String) = {
//    val fullObject = client.getObject(new GetObjectRequest(bucketName, objectKey));
//
//    val objectStream = fullObject.getObjectContent
//
//    val reader = new BufferedReader(new InputStreamReader(objectStream))
//
//    var str: String = reader.readLine()
//    do {
//      println(str)
//      str = reader.readLine()
//    } while(str != null)
//  }
//
//
//  def createObject(objectKey: String, filename: String): Unit = {
//
//    val request = new PutObjectRequest(bucketName, objectKey, new File(filename))
//
//    val metadata = new ObjectMetadata()
//
//    metadata.setContentType("plain/text")
//    metadata.addUserMetadata("user-type", "customer")
//
//    request.setMetadata(metadata)
//    client.putObject(request)
//  }

//  createBucket()
//  getObject("test.txt")
//  createObject("my/test/attpemt/file.txt", "./src/main/resources/hello.txt")

}
