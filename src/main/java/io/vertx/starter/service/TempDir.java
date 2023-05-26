package io.vertx.starter.service;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;

public class TempDir implements AutoCloseable {

  private final Path path;

  private TempDir(Path path) {
    this.path = path;
  }

  public Path path() {
    return path;
  }

  @Override
  public void close() throws Exception {
    Files.walkFileTree(path, new SimpleFileVisitor<>() {
      @Override
      public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
        Files.delete(file);
        return FileVisitResult.CONTINUE;
      }

      @Override
      public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
        Files.delete(dir);
        return FileVisitResult.CONTINUE;
      }
    });
  }

  public static TempDir create() throws IOException {
    return new TempDir(Files.createTempDirectory("starter"));
  }
}
