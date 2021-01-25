package net.slozzer.babel

import cats.effect.{Blocker, ContextShift, Sync}

abstract class Loader[F[_]] {
  def load(base: String, locales: Set[Locale]): F[Translations[Babel]]
}

object Loader {
  def default[F[_]: Sync: ContextShift](blocker: Blocker): Loader[F] = HoconLoader[F](blocker)
}