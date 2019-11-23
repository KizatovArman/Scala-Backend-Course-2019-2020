
import spray.json.DefaultJsonProtocol
import model._

trait SprayJsonSerializer extends DefaultJsonProtocol{

  implicit val pathFormat = jsonFormat1(Path)
  implicit val successfulResponse = jsonFormat2(SuccessfulResponse)
  implicit val errorResponse = jsonFormat2(ErrorResponse)
}
