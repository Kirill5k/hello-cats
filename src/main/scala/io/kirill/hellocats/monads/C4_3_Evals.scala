package io.kirill.hellocats.monads

object C4_3_Evals extends App {

  import cats.Eval

  val now = Eval.now(math.random + 1000)
  val later = Eval.later(math.random + 2000)
  val always = Eval.always(math.random + 3000)

  val greeting = Eval.always{ println("step 1"); "Hello"}
    .map { str => println("step 2"); s"$str world" }
//  println(greeting.value)

  val ans = for {
    a <- Eval.later{ println("calculating A"); 40 }
    b <- Eval.now{ println("calculating B"); 2 }
  } yield {
    println("adding a and b")
    a + b
  }
  println(ans.value)

  def factorial(n: BigInt): BigInt = if (n==1) n else n * factorial(n-1)

  def factorialViaEval(n: BigInt): Eval[BigInt] = if (n==1) Eval.now(n) else Eval.defer(factorialViaEval(n-1).map(_ * n))

  def foldRight[A, B](as: List[A], acc: B)(fn: (A, B) => B): B = as match {
    case head :: tail => fn(head, foldRight(tail, acc)(fn))
    case Nil => acc
  }

  def foldRightViaEval[A, B](as: List[A], acc: Eval[B])(fn: (A, Eval[B]) => Eval[B]): Eval[B] = as match {
    case head :: tail => Eval.defer(fn(head, foldRightViaEval(tail, acc)(fn)))
    case Nil => acc
  }

  def foldRight2[A, B](as: List[A], acc: B)(fn: (A, B) => B): B = foldRightViaEval(as, Eval.now(acc)) {
    (a, b) => b.map(fn(a, _))
  }.value
}
