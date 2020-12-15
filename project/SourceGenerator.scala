import java.util.Locale

object SourceGenerator {
  val locales: Array[Locale] = Locale.getAvailableLocales
    .filter { locale =>
      val label = locale.toString
      label.nonEmpty && !label.contains("#") && label.length <= 5
    }

  def identifier(value: String): String = value.toLowerCase.capitalize

  def languages(pkg: String): String = {
    val languages = locales.map(_.getLanguage).toSet
    val vals = languages.map { language => s"""val ${identifier(language)}: Language = Language("$language")""" }
    val all = languages.map(identifier).mkString(", ")

    s"""package $pkg
       |
       |import $pkg.Language
       |
       |object Languages {
       |  // $$COVERAGE-OFF$$
       |  ${vals.mkString("\n\n  ")}
       |
       |  val All: List[Language] = List($all)
       |  // $$COVERAGE-ON$$
       |}""".stripMargin
  }

  def countries(pkg: String): String = {
    val countries = locales.map(_.getCountry).filter(_.nonEmpty).toSet
    val vals = countries.map { country => s"""val ${identifier(country)}: Country = Country("$country")""" }
    val all = countries.map(identifier).mkString(", ")

    s"""package $pkg
       |
       |import $pkg.Country
       |
       |object Countries {
       |  // $$COVERAGE-OFF$$
       |  ${vals.mkString("\n\n  ")}
       |
       |  val All: List[Country] = List($all)
       |  // $$COVERAGE-ON$$
       |}""".stripMargin
  }

  def locales(pkg: String): String = {
    val vals = locales.map { locale =>
      val language = locale.getLanguage
      val country = locale.getCountry

      if (country.isEmpty) {
        s"val $locale: Locale = Locale(Languages.${identifier(language)})"
      } else {
        s"val $locale: Locale = Locale(" +
          s"Languages.${identifier(language)}, " +
          s"Some(Countries.${identifier(country)}))"
      }
    }
    val all = locales.map(_.toString).mkString(", ")

    s"""package $pkg
       |
       |import $pkg.Locale
       |
       |object Locales {
       |  // $$COVERAGE-OFF$$
       |  ${vals.mkString("\n\n  ")}
       |
       |  val All: List[Locale] = List($all)
       |  // $$COVERAGE-ON$$
       |}""".stripMargin
  }
}
