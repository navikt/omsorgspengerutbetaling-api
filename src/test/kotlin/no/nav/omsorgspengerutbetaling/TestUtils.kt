package no.nav.omsorgspengerutbetaling

import com.auth0.jwt.JWT
import com.github.tomakehurst.wiremock.http.Cookie
import com.github.tomakehurst.wiremock.http.Request
import io.ktor.http.*
import no.nav.helse.dusseldorf.ktor.core.Throwblem
import no.nav.helse.dusseldorf.testsupport.jws.LoginService
import no.nav.omsorgspengerutbetaling.felles.somJson
import no.nav.omsorgspengerutbetaling.soknad.Søknad
import no.nav.omsorgspengerutbetaling.soknad.valider
import org.json.JSONArray
import org.skyscreamer.jsonassert.JSONAssert
import kotlin.test.assertTrue

class TestUtils {
    companion object {
        fun getIdentFromIdToken(request: Request?): String {
            val idToken: String = request!!.getHeader(HttpHeaders.Authorization).substringAfter("Bearer ")
            return JWT.decode(idToken).subject ?: throw IllegalStateException("Token mangler 'sub' claim.")
        }

        fun validerOgAssertMangler(søknad: Søknad, skalGiMangler: Boolean, forventetMangler: String? = null){
            try {
                søknad.valider()

                //Hvis valider kaster Throwblem blir ikke linjen under kjørt
                if(skalGiMangler) assertTrue(false, "Forventet at validering skulle gi mangler. Gjorde ikke det.")
            } catch (throwblem: Throwblem){
                if(!skalGiMangler) assertTrue(false, "Forventet ikke at validering skulle gi mangler, men gjorde det. Feil=${throwblem.getProblemDetails().somJson()}")
                if (skalGiMangler && forventetMangler == null) assertTrue(false, "Forventet at validering skulle gi mangler, men har ikke oppgitt forventetMangler.")

                val forventetManglerJSON = JSONArray(forventetMangler)
                val faktiskeMangler = JSONArray(throwblem.getProblemDetails().asMap()["invalid_parameters"]!!.somJson())
                println(faktiskeMangler)
                JSONAssert.assertEquals(forventetManglerJSON, faktiskeMangler, true)
            }
        }

        fun getAuthCookie(
            fnr: String,
            level: Int = 4,
            cookieName: String = "localhost-idtoken",
            expiry: Long? = null) : Cookie {

            val overridingClaims : Map<String, Any> = if (expiry == null) emptyMap() else mapOf(
                "exp" to expiry
            )

            val jwt = LoginService.V1_0.generateJwt(fnr = fnr, level = level, overridingClaims = overridingClaims)
            return Cookie(listOf(String.format("%s=%s", cookieName, jwt), "Path=/", "Domain=localhost"))
        }
    }
}