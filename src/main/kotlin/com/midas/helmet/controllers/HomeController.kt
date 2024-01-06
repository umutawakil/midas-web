package com.midas.helmet.controllers

import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpSession
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.ui.set
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam

@Controller
class HomeController {
    @GetMapping("/")
    fun index(
        model: Model,
        @RequestParam(required = false) c: String?,
       // @RequestParam(required = false) o: Discussion.DiscussionId?,
        @RequestParam(required = false) d: Int?,
        httpSession: HttpSession,
        request: HttpServletRequest
    ): String {
        //val language: Language = httpSession.getAttribute("language") as Language
        //val countryDisplayName = httpSession.getAttribute("countryDisplayName") as CountryDisplayName

        //model["languageDisplayNames"] = languageService.getAllLanguageDisplayNames(language = language)
        //model["bodyTranslations"]     = translationService.getTranslationMap(context = "homepage", language)
        //model["discussions"]          = page.values

        //WebUtil.safeSetModelAttribute(model,"nextOffsetKey", page.nextOffsetKey)
        //WebUtil.safeSetModelAttribute(model,"previousOffsetKey", page.previousOffsetKey)

        return "index"
    }
}