package jatatui.components.theme;

import static jatatui.components.Components.themeProvider;
import static jatatui.react.Components.component;
import static jatatui.react.Components.text;
import static org.junit.jupiter.api.Assertions.*;

import jatatui.react.Element;
import jatatui.react.TestHarness;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.Test;

class ThemeTest {

  @Test
  void default_theme_is_light_when_no_provider() throws IOException {
    AtomicReference<Theme> seen = new AtomicReference<>();
    Element app =
        component(
            ctx -> {
              seen.set(Theme.useTheme(ctx));
              return text("ok");
            });
    render(app);
    assertEquals(Theme.LIGHT, seen.get());
  }

  @Test
  void provider_overrides_default() throws IOException {
    AtomicReference<Theme> seen = new AtomicReference<>();
    Element child =
        component(
            ctx -> {
              seen.set(Theme.useTheme(ctx));
              return text("ok");
            });
    Element app = themeProvider(Theme.DARK, child);
    render(app);
    assertEquals(Theme.DARK, seen.get());
  }

  static void render(Element root) throws IOException {
    new TestHarness(40, 12).render(root);
  }
}
