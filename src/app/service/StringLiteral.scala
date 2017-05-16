package service

import org.apache.jena.rdf.model.Literal

case class StringLiteral(value: String, language: Option[String])

object StringLiteral {
    def create(literal: Literal) = {
        val language = Option(literal.getLanguage).filter(_.trim.nonEmpty)
        StringLiteral(literal.getString, language)
    }
}