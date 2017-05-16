package service

import service.model.rdf.extractor.LabelExtractor

import scalaj.http.{Http, HttpOptions}

class DereferenceService {

    def dereferenceLabels(uri: String) = {
        val query = new LabelDereferenceQuery(uri)
    }

    private def dereference(uri: String, query: LabelDereferenceQuery, extractor: LabelExtractor): Option[R] = {
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
                    Graph(ttl).flatMap(g => extractor.extract(QueryExecutionFactory.create(query.get, g.jenaModel)))
                }
            }
        } catch {
            case qEx: QueryExceptionHTTP => throw qEx
            case e => println(e); None
        }
    }

}
