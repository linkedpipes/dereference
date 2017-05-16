package service

import org.apache.jena.query.QueryExecutionFactory
import org.apache.jena.rdf.model.{Model, ModelFactory}
import java.io.StringReader

import org.apache.jena.sparql.engine.http.QueryExceptionHTTP

import scalaj.http.{Http, HttpOptions}

class DereferenceService {

    def dereferenceLabels(uri: String) : Option[LocalizedValue] = {
        val query = new LabelDereferenceQuery(uri)
        dereference(uri, query, new LabelExtractor)
    }

    private def dereference(uri: String, query: LabelDereferenceQuery, extractor: LabelExtractor): Option[LocalizedValue] = {
        try {
            val request = Http(uri)
                .timeout(connTimeoutMs = 10000, readTimeoutMs = 30000)
                .header("Accept", "text/turtle")
                .option(HttpOptions.followRedirects(true))

            val response = request.asString
            response.code match {
                case redirect if redirect == 303 => dereference(response.header("Location").get, query, extractor)
                case _ => {
                    val ttl = response.body
                    createModel(ttl).flatMap(m => extractor.extract(QueryExecutionFactory.create(query.value, m)))
                }
            }
        } catch {
            case qEx: QueryExceptionHTTP => throw qEx
            case e => println(e); None
        }
    }

    private def createModel(ttl: String) : Option[Model] = {
        try {
            val jenaModel = ModelFactory.createDefaultModel()
            jenaModel.read(new StringReader(ttl), null, "N3")
            Some(jenaModel)
        } catch {
            case e: Throwable => None
        }
    }

}
