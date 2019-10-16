package nl.vindh.tools.findusage

import java.io.FileInputStream

import jdk.internal.org.objectweb.asm.tree.ClassNode
import nl.vindh.tools.recfind.JarReader
import jdk.internal.org.objectweb.asm.{ClassReader}

import scala.util.Try

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
    } yield classFiles.map(_._1).zip(usageDatas)


  }

  def classUsesIdentifier(classFile: Array[Byte], ident: String): Boolean = {
    val cr = new ClassReader(classFile)
    val cn = new ClassNode()
    cr.accept(cn, 0)

    cn.

  }
}
