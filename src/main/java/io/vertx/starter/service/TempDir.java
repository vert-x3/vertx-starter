package io.vertx.starter.service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static java.util.Comparator.reverseOrder;

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
    try (var pathStream = Files.walk(path)) {
      pathStream.sorted(reverseOrder()).map(Path::toFile).forEach(File::delete);
    }
  }

  public static TempDir create() throws IOException {
    return new TempDir(Files.createTempDirectory("starter"));
  }
}
