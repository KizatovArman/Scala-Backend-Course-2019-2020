package ElasticSerializer

import Serializer.SprayJsonSerializer
import com.sksamuel.elastic4s.{Hit, HitReader, Indexable}
import model.Manga
import spray.json._

import scala.util.Try

trait ElasticSerializer extends SprayJsonSerializer{

  implicit object MangaIndexable extends Indexable[Manga] {
    override def json(manga: Manga): String = manga.toJson.compactPrint
  }

  // JSON string -> object
  // parseJson is a Spray method

  implicit object MovieHitReader extends HitReader[Manga] {
    override def read(hit: Hit): Either[Throwable, Manga] = {
      Try {
        val jsonAst = hit.sourceAsString.parseJson
        jsonAst.convertTo[Manga]
      }.toEither
    }
  }
}
