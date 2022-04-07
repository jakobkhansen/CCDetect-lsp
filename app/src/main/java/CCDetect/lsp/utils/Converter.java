package CCDetect.lsp.utils;

import java.util.Collection;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

public class Converter<T, U> {

  private final Function<T, U> fromDto;
  private final Function<U, T> fromEntity;

  public Converter(final Function<T, U> fromLeft, final Function<U, T> fromRight) {
    this.fromDto = fromLeft;
    this.fromEntity = fromRight;
  }

  public final U convertFromLeft(final T dto) {
    return fromDto.apply(dto);
  }

  public final T convertFromRight(final U entity) {
    return fromEntity.apply(entity);
  }

  public final List<U> createFromLeft(final Collection<T> dtos) {
    return dtos.stream().map(this::convertFromLeft).collect(Collectors.toList());
  }

  public final List<T> createFromRight(final Collection<U> entities) {
    return entities.stream().map(this::convertFromRight).collect(Collectors.toList());
  }
}

