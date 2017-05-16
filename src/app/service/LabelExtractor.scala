package service

import org.apache.jena.query.{QueryExecution, QuerySolution}
import scala.collection.JavaConversions._


class LabelExtractor {

    case class LabelResult(
        rdfsLabel: Option[StringLiteral],
        skosPrefLabel: Option[StringLiteral],
        skosNotation: Option[StringLiteral],
        schemaName: Option[StringLiteral],
        schemaTitle: Option[StringLiteral],
        dctermsTitle: Option[StringLiteral],
        legalName: Option[StringLiteral]
    )

    val selectors: Seq[(LabelResult => Option[StringLiteral])] = Seq(
        r => r.rdfsLabel,
        r => r.skosPrefLabel,
        r => r.schemaName,
        r => r.skosNotation,
        r => r.schemaTitle,
        r => r.dctermsTitle,
        r => r.legalName
    )

    def extract(data: QueryExecution): Option[LocalizedValue] = {
        val result = data.execSelect().asInstanceOf[java.util.Iterator[QuerySolution]]

        val labelResults = result.toList.map { qs =>
            LabelResult(
                rdfsLabel = Option(qs.get("l")).map(_.asLiteral()).map(StringLiteral.create),
                skosPrefLabel = Option(qs.get("spl")).map(_.asLiteral()).map(StringLiteral.create),
                skosNotation = Option(qs.get("sn")).map(_.asLiteral()).map(StringLiteral.create),
                schemaName = Option(qs.get("sna")).map(_.asLiteral()).map(StringLiteral.create),
                schemaTitle = Option(qs.get("st")).map(_.asLiteral()).map(StringLiteral.create),
                dctermsTitle = Option(qs.get("dct")).map(_.asLiteral()).map(StringLiteral.create),
                legalName = Option(qs.get("ln")).map(_.asLiteral()).map(StringLiteral.create)
            )
        }

        val allLanguages = labelResults.flatMap(r =>
            selectors.flatMap(s => s(r)).map(_.language)
        ).distinct

        val labels = allLanguages.map(l =>
            (l, findLabel(l, labelResults))
        )

        val map = labels.filter(_._2.isDefined).map {
            case (lang, maybeValue) => (lang, maybeValue.get.value)
        }

        map.isEmpty match {case false => Some(LocalizedValue.apply(map.toMap)) case _ => None}

    }

    private def findLabel(language: Option[String], available: Seq[LabelResult]): Option[StringLiteral] = {
        selectors.flatMap(s => available.flatMap(r => s(r)).find(_.language == language)).headOption
    }
}
