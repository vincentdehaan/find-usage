package nl.vindh.tools.findusage

import java.io.FileInputStream

import jdk.internal.org.objectweb.asm.tree.{ClassNode, MethodInsnNode}
import nl.vindh.tools.recfind.{JarReader} // TODO: change package name of JarReader
import jdk.internal.org.objectweb.asm.{ClassReader, Opcodes}

import scala.util.Try

case class Method(owner: String, name: String, desc: String) {
  override def toString: String = s"$owner/$name$desc"
}
object Main {
  def main(args: Array[String]): Unit = {
    val filename = args.head
    val ident = args.tail.head

    val tryClassFiles = for {
      fis <- Try(new FileInputStream(filename))
      classInfos <- JarReader.getClassFiles(fis)
      classFiles = classInfos.toList
      usageDatas = for {
        classFile <- classFiles.map(_._2)
      } yield classUsesIdentifier(classFile, ident)
    } yield usageDatas.flatten

  println(tryClassFiles.get.mkString("\n"))
  }

  def classUsesIdentifier(classFile: Array[Byte], ident: String): IndexedSeq[(Method, Method)] = {
    val cr = new ClassReader(classFile)
    val cn = new ClassNode()
    cr.accept(cn, 0)

    cn.methods.flatMap {
      method => {
        val caller = Method(cn.name, method.name, method.desc)
        method.instructions.toArray.filter {
          instruction =>
            instruction.getOpcode == Opcodes.INVOKEVIRTUAL ||
              instruction.getOpcode == Opcodes.INVOKEINTERFACE ||
              instruction.getOpcode == Opcodes.INVOKESPECIAL ||
              instruction.getOpcode == Opcodes.INVOKESTATIC
        }.map {
          instruction => {
            val instr = instruction.asInstanceOf[MethodInsnNode]
            val m = Method(instr.owner, instr.name, instr.desc)
            //println(s"Method call found: $m")
            m
          }
        }.filter {
          m => m.toString.contains(ident)
        }.map((caller, _))
      }
    }

  }

  implicit def utilListToIndexedSeq[A](xs: java.util.List[A]): IndexedSeq[A] =
    new IndexedSeq[A] {
      def apply(i: Int): A = xs.get(i)
      lazy val length = xs.size
    }
}
