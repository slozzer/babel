package io.taig.lokal

import scala.annotation.tailrec

final case class Segments[+A](branches: Map[String, Either[A, Segments[A]]]) {
  @inline
  def get(segment: String): Option[Either[A, Segments[A]]] = branches.get(segment)

  @tailrec
  def get(path: Path): Option[Either[A, Segments[A]]] = path match {
    case Path(Nil)         => None
    case Path(head :: Nil) => get(head)
    case Path(head :: tail) =>
      get(head) match {
        case Some(Right(segments)) => segments.get(Path(tail))
        case Some(Left(_))         => None
        case None                  => None
      }
  }

  @inline
  def isEmpty: Boolean = branches.isEmpty

  def getLeaf(path: Path): Option[A] = get(path).collect { case Left(value) => value }

  def map[B](f: A => B): Segments[B] =
    Segments(branches.view.mapValues {
      case Right(segments) => Right(segments.map(f))
      case Left(value)     => Left(f(value))
    }.toMap)

  def mapWithPath[B](f: (Path, A) => B): Segments[B] = {
    def go(path: Path, segments: Segments[A]): Segments[B] =
      Segments(segments.branches.map {
        case (segment, Right(segments)) => segment -> Right(go(path / segment, segments))
        case (segment, Left(value))     => segment -> Left(f(path / segment, value))
      })

    go(Path.Empty, this)
  }

  def mapFilter[B](f: A => Option[B]): Segments[B] =
    Segments(branches.view.foldLeft(Map.empty[String, Either[B, Segments[B]]]) {
      case (result, (key, Right(segments))) =>
        val update = segments.mapFilter(f).branches
        if (update.isEmpty) result else result + (key -> Right(Segments(update)))
      case (result, (key, Left(value))) => f(value).fold(result)(value => result + (key -> Left(value)))
    })

  def forall(f: A => Boolean): Boolean = branches.forall {
    case (_, Right(segments)) => segments.forall(f)
    case (_, Left(value))     => f(value)
  }

  def ++[A1 >: A](segments: Segments[A1]): Segments[A1] = Segments(branches ++ segments.branches)

  // TODO tailrec and error message
  def merge[A1 >: A](segments: Segments[A1])(f: (A1, A1) => A1): Either[String, Segments[A1]] =
    branches
      .foldLeft[Either[String, Map[String, Either[A1, Segments[A1]]]]](Right(segments.branches)) {
        case (Right(branches), pair @ (key, left)) =>
          branches.get(key) match {
            case Some(right) =>
              (left, right) match {
                case (Left(left), Left(right)) => Right(branches + (key -> Left(f(left, right))))
                case (Right(left), Right(right)) =>
                  left.merge(right)(f).map(result => branches + (key -> Right(result)))
                case _ => Left("Can not merge a node with a leaf")
              }
            case None => Right(branches + pair)
          }
        case (error @ Left(_), _) => error
      }
      .map(Segments[A1])

  // TODO tailrec
  def toMap: Map[Path, A] = {
    val builder = Map.newBuilder[Path, A]

    def go(path: Path, branches: Map[String, Either[A, Segments[A]]]): Unit =
      branches.foreach {
        case (segment, Left(value))     => builder.addOne(path / segment -> value)
        case (segment, Right(segments)) => go(path / segment, segments.branches)
      }

    go(Path.Empty, branches)

    builder.result()
  }
}

object Segments {
  val Empty: Segments[Nothing] = Segments(Map.empty)

  def one[A](segment: String, value: A): Segments[A] = Segments(Map(segment -> Left(value)))

  def of[A](values: (String, A)*): Segments[A] = Segments(values.toMap.view.mapValues(Left.apply).toMap)
}
