package service

class LabelDereferenceQuery(val uri: String) {

    def value: String =
        s"""
           | PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>
           | PREFIX skos: <http://www.w3.org/2004/02/skos/core#>
           | PREFIX schema: <http://schema.org/>
           | PREFIX dcterms: <http://purl.org/dc/terms/>
           | PREFIX gr: <http://purl.org/goodrelations/v1#>
           |
      | SELECT DISTINCT ?l ?spl ?sn ?sna ?st ?ln
           | WHERE {
           |     OPTIONAL { <$uri> rdfs:label ?l . }
           |     OPTIONAL { <$uri> skos:prefLabel ?spl . }
           |     OPTIONAL { <$uri> skos:notation ?sn . }
           |     OPTIONAL { <$uri> schema:name ?sna . }
           |     OPTIONAL { <$uri> schema:title ?st . }
           |     OPTIONAL { <$uri> dcterms:title ?st . }
           |     OPTIONAL { <$uri> gr:legalName ?ln . }
           | }
    """.stripMargin

}