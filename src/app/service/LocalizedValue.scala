package service

import play.api.libs.json.{Json, Writes}
import scala.collection.mutable
import scala.collection.JavaConversions._

class LocalizedValue {

    private val languageMap = new mutable.HashMap[Option[String], String]

    def put(language: Option[String], localizedString: String) {
        languageMap.put(language, localizedString)
    }

    def get(language: Option[String]): Option[String] = {
        languageMap.get(language)
    }

    def getOrElse(language: Option[String], fallbackLanguage: Option[String]): Option[String] = {
        findFirstDefined(Seq(language, fallbackLanguage))(k => languageMap.get(k))
    }

    def getOrFirst(language: Option[String]): Option[String] = {
        findFirstDefined(Seq(language, languageMap.keys.head))(k => languageMap.get(k))
    }

    def size: Int = languageMap.size

    private def findFirstDefined[A, B](coll: Traversable[A])(f: A => Option[B]): Option[B] = {
        coll.collectFirst(Function.unlift(f))
    }
}

object LocalizedValue {

    implicit val writes : Writes[LocalizedValue] = new Writes[LocalizedValue] {
        override def writes(lv: LocalizedValue) = {
            Json.toJson(lv.languageMap.map {
                case (key, value) => key.toString -> value
            }.toMap)
        }
    }

    def apply(variants: Map[Option[String], String]): LocalizedValue = {
        val l = new LocalizedValue
        variants.foreach { case (language, value) =>
            l.put(language, value)
        }
        l
    }

    def create(variant: (Option[String], String)): LocalizedValue = {
        apply(Seq(variant).toMap)
    }

    def create(literal: org.apache.jena.rdf.model.Literal): LocalizedValue = {
        create((Option(literal.getLanguage), literal.getString))
    }

    def create(literals: Seq[org.apache.jena.rdf.model.Literal]): LocalizedValue = {
        apply(literals.map(l => (Option(l.getLanguage), l.getString)).toMap)
    }

    def create(resource: org.apache.jena.rdf.model.Resource, property: org.apache.jena.rdf.model.Property): LocalizedValue = {
        apply(resource.listProperties(property).toList
            .map(_.getObject.asLiteral()).reverse
            .map(l => (Option(l.getLanguage), l.getString)).toMap)
    }

    def unapply(l: LocalizedValue): Option[Map[Option[String], String]] = {
        if (l.languageMap.nonEmpty) {
            Some(l.languageMap.toMap)
        } else {
            None
        }
    }

}
