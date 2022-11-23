package tui.crossterm;

public record Duration (
        long secs,
        // Always 0 <= nanos < NANOS_PER_SEC
        int nanos 
) {}

