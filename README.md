# omsorgspengerutbetaling-api
![CI / CD](https://github.com/navikt/omsorgspengerutbetaling-api/workflows/CI%20/%20CD/badge.svg)

# Innholdsoversikt
* [1. Kontekst](#1-kontekst)
* [2. Funksjonelle Krav](#2-funksjonelle-krav)
* [3. Begrensninger](#3-begrensninger)
* [4. Prinsipper](#4-prinsipper)
* [5. Programvarearkitektur](#5-programvarearkitektur)
* [6. Kode](#6-kode)
* [7. Data](#7-data)
* [8. Infrastrukturarkitektur](#8-infrastrukturarkitektur)
* [9. Distribusjon av tjenesten (deployment)](#9-distribusjon-av-tjenesten-deployment)
* [10. Utviklingsmiljø](#10-utviklingsmilj)
* [11. Drift og støtte](#11-drift-og-sttte)

# 1. Kontekst
API - tjeneste for omsorgspengerutbetaling

# 2. Funksjonelle Krav
Denne tjenesten understøtter søknadsprosessen, samt eksponerer endepunkt for innsending av søknad om utvidet rett til omsorgspengerutbetaling.



# 3. Begrensninger

# 4. Prinsipper

# 5. Programvarearkitektur

# 6. Kode

# 7. Data
## Full Søknad - Innkommende
````json
{
  "språk": "nb",
  "bosteder": [
    {
      "fraOgMed": "2020-02-28",
      "tilOgMed": "2020-03-09",
      "landkode": "GB",
      "landnavn": "Great Britain"
    }
  ],
  "opphold": [
    {
      "fraOgMed": "2020-02-28",
      "tilOgMed": "2020-03-09",
      "landkode": "GB",
      "landnavn": "Great Britain"
    }
  ],
  "spørsmål": [
    {
      "id": "HarBekreftetOpplysninger",
      "spørsmål": "HarBekreftetOpplysninger?",
      "svar": "Ja",
      "fritekst": null
    },
    {
      "id": "HarForståttRettigheterOgPlikter",
      "spørsmål": "HarForståttRettigheterOgPlikter?",
      "svar": "Ja",
      "fritekst": null
    }
  ],
  "utbetalingsperioder": [
    {
      "fraOgMed": "2020-03-19",
      "tilOgMed": "2020-03-24",
      "lengde": null,
      "legeerklæringer": ["http://localhost:8080/vedlegg/1","http://localhost:8080/vedlegg/2"]
    }
  ],
  "harHattInntektSomFrilanser": true,
  "frilans": {
    "startdato": "2020-03-19",
    "jobberFortsattSomFrilans": true
  },
  "harHattInntektSomSelvstendigNaringsdrivende": true,
  "selvstendigVirksomheter": [
    {
      "naringstype": [
        "JORDBRUK_SKOGBRUK"
      ],
      "fiskerErPåBladB": null,
      "fraOgMed": "2020-03-18",
      "tilOgMed": "2020-03-19",
      "erPagaende": false,
      "naringsinntekt": 123123,
      "navnPaVirksomheten": "TullOgTøys",
      "organisasjonsnummer": "101010",
      "registrertINorge": true,
      "registrertILand": null,
      "harBlittYrkesaktivSisteTreFerdigliknendeArene": null,
      "yrkesaktivSisteTreFerdigliknedeArene": {
        "oppstartsdato": "2020-03-19"
      },
      "harVarigEndringAvInntektSiste4Kalenderar": false,
      "varigEndring": null,
      "harRegnskapsforer": true,
      "regnskapsforer": {
        "navn": "Kjell",
        "telefon": "84554",
        "erNarVennFamilie": false
      },
      "harRevisor": false,
      "revisor": null
    }
  ]
}
````

## Full Søknad - Utgående
````json
{
  "mottatt": "2020-03-19T16:08:37.549739+01:00",
  "språk": "nb",
  "søker": {
    "aktørId": "123456",
    "fødselsdato": "1999-11-02",
    "fødselsnummer": "02119970078",
    "fornavn": "Ola",
    "mellomnavn": null,
    "etternavn": "Nordmann",
    "myndig": true
  },
  "bosteder": [
    {
      "fraOgMed": "2020-02-28",
      "tilOgMed": "2020-03-09",
      "landkode": "GB",
      "landnavn": "Great Britain"
    }
  ],
  "opphold": [
    {
      "fraOgMed": "2020-02-28",
      "tilOgMed": "2020-03-09",
      "landkode": "GB",
      "landnavn": "Great Britain"
    }
  ],
  "spørsmål": [
    {
      "id": "HarBekreftetOpplysninger",
      "spørsmål": "HarBekreftetOpplysninger?",
      "svar": "Ja",
      "fritekst": null
    },
    {
      "id": "HarForståttRettigheterOgPlikter",
      "spørsmål": "HarForståttRettigheterOgPlikter?",
      "svar": "Ja",
      "fritekst": null
    }
  ],
  "utbetalingsperioder": [
    {
      "fraOgMed": "2020-03-19",
      "tilOgMed": "2020-03-24",
      "lengde": "PT7H30M"
    }
  ],
  "vedlegg": [
    {
      "content": "ZGV0dGUgZXIgZXQgYmlsZGUgOnA=",
      "contentType": "img/pdf",
      "title": "Navn på fil"
    }
  ],
  "frilans": {
    "startdato": "2020-03-19",
    "jobberFortsattSomFrilans": true
  },
  "selvstendigVirksomheter": [
    {
      "naringstype": [
        "JORDBRUK_SKOGBRUK"
      ],
      "fiskerErPåBladB": null,
      "fraOgMed": "2020-03-18",
      "tilOgMed": "2020-03-19",
      "erPagaende": false,
      "naringsinntekt": 123123,
      "navnPaVirksomheten": "TullOgTøys",
      "organisasjonsnummer": "101010",
      "registrertINorge": true,
      "registrertILand": null,
      "harBlittYrkesaktivSisteTreFerdigliknendeArene": null,
      "yrkesaktivSisteTreFerdigliknedeArene": {
        "oppstartsdato": "2020-03-19"
      },
      "harVarigEndringAvInntektSiste4Kalenderar": false,
      "varigEndring": null,
      "harRegnskapsforer": true,
      "regnskapsforer": {
        "navn": "Kjell",
        "telefon": "84554",
        "erNarVennFamilie": false
      },
      "harRevisor": false,
      "revisor": null
    }
  ]
}
````

# 8. Infrastrukturarkitektur

# 9. Distribusjon av tjenesten (deployment)
Distribusjon av tjenesten er gjort med bruk av Github Actions.
[Omsorgspengerutbetaling-API CI / CD](https://github.com/navikt/omsorgspengesoknadutbetaling-api/actions)

Push til dev-* brancher vil teste, bygge og deploye til dev/staging miljø.
Push/merge til master branche vil teste, bygge og deploye til produksjonsmiljø.

# 10. Utviklingsmiljø
## Bygge Prosjekt
For å bygge kode, kjør:

```shell script
./gradlew clean build
```

## Kjøre Prosjekt
For å kjøre kode, kjør:

```shell script
./gradlew bootRun
```

# 11. Drift og støtte
## Logging
[Kibana](https://tinyurl.com/ydkqetfo)

# Metrics
n/a

### Redis
Vi bruker Redis for mellomlagring. En instanse av Redis må være kjørene før deploy av applikasjonen. 
Dette gjøres manuelt med kubectl både i preprod og prod. Se [nais/doc](https://github.com/nais/doc/blob/master/content/redis.md)

1. `kubectl config use-context preprod-sbs`
2. `kubectl apply -f redis-config.yml`
