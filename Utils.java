import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

public class Utils {
    public static final DateTimeFormatter BR = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    public static Optional<LocalDateTime> tryParse(String texto) {
        try {
            return Optional.of(LocalDateTime.parse(texto, BR));
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    public static String fmt(LocalDateTime dt) {
        return dt.format(BR);
    }
}
