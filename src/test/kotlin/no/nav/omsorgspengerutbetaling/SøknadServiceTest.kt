package no.nav.omsorgspengerutbetaling

import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.RelaxedMockK
import kotlinx.coroutines.runBlocking
import no.nav.helse.dusseldorf.testsupport.jws.Azure
import no.nav.omsorgspengerutbetaling.general.CallId
import no.nav.omsorgspengerutbetaling.general.auth.IdToken
import no.nav.omsorgspengerutbetaling.kafka.KafkaProducer
import no.nav.omsorgspengerutbetaling.kafka.Metadata
import no.nav.omsorgspengerutbetaling.soker.Søker
import no.nav.omsorgspengerutbetaling.soker.SøkerService
import no.nav.omsorgspengerutbetaling.soknad.MeldingRegistreringFeiletException
import no.nav.omsorgspengerutbetaling.soknad.SøknadService
import no.nav.omsorgspengerutbetaling.vedlegg.DokumentEier
import no.nav.omsorgspengerutbetaling.vedlegg.Vedlegg
import no.nav.omsorgspengerutbetaling.vedlegg.VedleggService
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.assertThrows
import java.net.URI
import java.net.URL
import java.time.LocalDate
import kotlin.test.Test

internal class SøknadServiceTest{
    @RelaxedMockK
    lateinit var kafkaProducer: KafkaProducer

    @RelaxedMockK
    lateinit var søkerService: SøkerService

    @RelaxedMockK
    lateinit var vedleggService: VedleggService

    lateinit var søknadService: SøknadService

    @BeforeEach
    internal fun setUp() {
        MockKAnnotations.init(this)
        søknadService = SøknadService(
            søkerService = søkerService,
            kafkaProducer = kafkaProducer,
            k9MellomlagringIngress = URI("http://localhost:8080/v1/dokument"),
            vedleggService = vedleggService
        )
        assertNotNull(kafkaProducer)
        assertNotNull(søknadService)
    }

    @Test
    internal fun `Tester at den fjerner hold på persistert vedlegg dersom kafka feiler`() {
        assertThrows<MeldingRegistreringFeiletException> {
            runBlocking {
                coEvery {søkerService.getSoker(any(), any()) } returns Søker(
                    aktørId = "123",
                    fødselsdato = LocalDate.parse("2000-01-01"),
                    fødselsnummer = "290990123456"
                )

                coEvery {vedleggService.hentVedlegg(vedleggUrls = any(), any(), any(), any()) } returns listOf(Vedlegg("bytearray".toByteArray(), "vedlegg", "vedlegg", DokumentEier("290990123456")))

                every { kafkaProducer.produserKafkaMelding(any(), any()) } throws Exception("Mocket feil ved kafkaProducer")

                søknadService.registrer(
                    søknad = SøknadUtils.hentGyldigSøknad().copy(
                        vedlegg = listOf(URL("https://www.vg.no/vedlegg"))
                    ),
                    metadata = Metadata(
                        version = 1,
                        correlationId = "123"
                    ),
                    idToken = IdToken(Azure.V2_0.generateJwt(clientId = "ikke-authorized-client", audience = "omsorgsdager-melding-api")),
                    callId = CallId("abc")
                )
            }
        }

        coVerify(exactly = 1) { vedleggService.fjernHoldPåPersistertVedlegg(any(), any(), any()) }
    }
}