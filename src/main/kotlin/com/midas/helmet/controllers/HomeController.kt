package com.midas.helmet.controllers

import com.midas.helmet.domain.StockInfo
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpSession
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.ui.set
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestParam

@Controller
class HomeController {
    @GetMapping("/")
    fun index(
        model: Model
    ): String {
        model["stocks"] = StockInfo.find()
        //val language: Language = httpSession.getAttribute("language") as Language
        //val countryDisplayName = httpSession.getAttribute("countryDisplayName") as CountryDisplayName

        //model["languageDisplayNames"] = languageService.getAllLanguageDisplayNames(language = language)
        //model["bodyTranslations"]     = translationService.getTranslationMap(context = "homepage", language)
        //model["discussions"]          = page.values

        //WebUtil.safeSetModelAttribute(model,"nextOffsetKey", page.nextOffsetKey)
        //WebUtil.safeSetModelAttribute(model,"previousOffsetKey", page.previousOffsetKey)

        return "index"
    }

    @PostMapping("/")
    fun homePagePost(
        model: Model,
        @RequestParam(name="profitablity", required = false) profitability: Boolean?,
        @RequestParam(name="time_window", required = false)  timeWindow: Int?,
        @RequestParam(name="volatility_limit", required = false)  volatilityLimit: Int?,
        httpSession: HttpSession,
        request: HttpServletRequest
    ): String {
         model["stocks"] = StockInfo.find()

        //WebUtil.safeSetModelAttribute(model,"nextOffsetKey", page.nextOffsetKey)
        //WebUtil.safeSetModelAttribute(model,"previousOffsetKey", page.previousOffsetKey)

        return "index"
    }
}