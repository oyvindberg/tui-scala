package jatatui.tests.widgets.canvas;

import static org.junit.jupiter.api.Assertions.assertEquals;

import jatatui.core.buffer.Buffer;
import jatatui.core.layout.Rect;
import jatatui.core.style.Color;
import jatatui.core.symbols.Marker;
import jatatui.tests._support.BufferAssertions;
import jatatui.widgets.canvas.Canvas;
import jatatui.widgets.canvas.Map;
import jatatui.widgets.canvas.MapResolution;
import org.junit.jupiter.api.Test;

public class MapTest {

  // Upstream `map_resolution_to_string` — Java enum names match.
  @Test
  void map_resolution_to_string() {
    assertEquals("Low", MapResolution.Low.name());
    assertEquals("High", MapResolution.High.name());
  }

  // Upstream `map_resolution_from_str` — Java enum `valueOf` mirrors `parse`.
  @Test
  void map_resolution_from_str() {
    assertEquals(MapResolution.Low, MapResolution.valueOf("Low"));
    assertEquals(MapResolution.High, MapResolution.valueOf("High"));
    try {
      MapResolution.valueOf("");
      throw new AssertionError("expected IllegalArgumentException");
    } catch (IllegalArgumentException expected) {
      // expected
    }
  }

  // Upstream `default` — verifies factory defaults.
  @Test
  void map_default() {
    Map map = Map.empty();
    assertEquals(MapResolution.Low, map.resolution());
    assertEquals(Color.RESET, map.color());
  }

  // Upstream `draw_low` — render a low-res map and compare to the expected buffer.
  @Test
  void draw_low() {
    Buffer buffer = Buffer.empty(new Rect(0, 0, 80, 40));
    Canvas canvas =
        Canvas.empty()
            .withMarker(Marker.Dot)
            .withXBounds(new double[] {-180.0, 180.0})
            .withYBounds(new double[] {-90.0, 90.0})
            .withPaintFn(ctx -> ctx.draw(Map.empty()));
    canvas.render(buffer.area(), buffer);
    Buffer expected =
        Buffer.withLines(
            "                                                                                ",
            "                               •                                                ",
            "               • •• •••••••• ••   ••••    •••••  ••• ••     •••                 ",
            "             •••••••••••••••       •      ••••      • •   •••••••     •••       ",
            "    • •••• ••••••••••••••• ••     ••  •     •••    ••  ••••    ••  ••••••• •••  ",
            "•••••     •••••••••••• •••• •  ••••••     •••• • ••• •••••                     •",
            "   ••  • •   •••• ••••••••  ••••   ••  • •• •  •••                        •• •••",
            "    •••• •••   •••••• •••••   •       •• ••••••                       • •••••   ",
            "•••••     •••     •  ••   ••         •••••••                          ••  •• •• ",
            "            ••    ••••  •••••          ••       •  • •                ••        ",
            "            •  •    •••••••           •• •••• ••• •• •  ••          • ••        ",
            "            •          ••             ••••••••• • ••             •••• •         ",
            "             ••       ••              • • • •• •                  •••••         ",
            "              ••   •••               •      ••••  •               • •           ",
            "               •  •   ••             •         ••  •• •           •             ",
            "    ••          • •••••••           •           •   •  •   •   •• •             ",
            "                 •••••••••          •           •• •   •  • •• •  ••            ",
            "                    ••  ••          •            •••     •   •••  ••            ",
            "                     •••  • •        •  •         •     ••  •••  •••            ",
            "                      •               •  ••                   • ••              ",
            "                   •  •     •••                • •            •••   •••         ",
            "                                •         •     •              • •    •••       ",
            "  •                                        •    • •                  • • •      ",
            "                       •       •                • •               ••• ••       •",
            "                        •      •          •    • ••              •      •   •   ",
            "                        •    •                   •               •       •      ",
            "                        •   •              •   •                    •           ",
            "                           ••               ••                   ••  ••  •   •  ",
            "                       •  •                                           •••    •• ",
            "                       •  •                                            ••   ••  ",
            "                       • •                                                      ",
            "                       •••••                                                    ",
            "                                                                                ",
            "                          ••                                                    ",
            "                         •••           •       • ••••• • •••• • • •• •• ••      ",
            "            •    • • ••••••        ••••••••• • ••      ••                  •••  ",
            "•    ••• •••• ••••   • •  • ••• • •                                        ••• •",
            "   •• •                •  ••  • ••                                         ••   ",
            "•      •                                                                      • ",
            "                                                                                ");
    BufferAssertions.assertBufferEq(buffer, expected);
  }

  // Upstream `draw_high` — render a high-res map using Braille markers.
  @Test
  void draw_high() {
    Buffer buffer = Buffer.empty(new Rect(0, 0, 80, 40));
    Canvas canvas =
        Canvas.empty()
            .withMarker(Marker.Braille)
            .withXBounds(new double[] {-180.0, 180.0})
            .withYBounds(new double[] {-90.0, 90.0})
            .withPaintFn(ctx -> ctx.draw(Map.empty().withResolution(MapResolution.High)));
    canvas.render(buffer.area(), buffer);
    Buffer expected =
        Buffer.withLines(
            "                                                                                ",
            "                   ⢀⣀⣤⠄⠤⠤⣤⣀⡀⣀⣀⡄⠄⢄⣀⣄⡄⢀⡀                                          ",
            "             ⢀⣀⣤⠰⢤⣼⡯⢽⡟⣀⢶⣺⡛⠁       ⠈⢰⠃⠁    ⢖⣒⣾⡟⠂  ⠈⠛⠁        ⠺⢩⢖⡄                ",
            "            ⡬⢍⣿⣟⣿⣻⣿⣿⣿⡾⣯⡀⠈⠁⠁⢦      ⢀⡿       ⠈       ⢠⢶⠘⠋⡁⣀⢠⠤⠖⠘⠉⠁⠈⠼⡧⡄⣄⡀ ⢫⣗⠒⠆      ",
            "⣓  ⣠⠖⠓⠒⠢⠤⢄⠤⠶⠽⠽⣶⣃⣽⡮⣿⡷⣗⣤⡭⣍⢓⡄ ⠸⣷   ⢀⣀⠿⠇       ⢀⠔⠒⠲⠄⢄⢀⡀⢙⣑⡄⠴⡍⣟⠉          ⠑⠉⠉  ⠑⠐⠦⠤⣤⠤⢞",
            "⠶⢧⣗⢾⡆         ⠈⠈⠁⠈⠉⢀⣹⣶⣩⣽⣐⢮⠃ ⣇ ⢀⡔⠊ ⢰⣖⣲    ⢀⡐⠁⣰⠦ ⢲⣶⠛⠋    ⠐⠋                      ⡤",
            "  ⠉⣮⣀⣀⣴⡤⣠⡀         ⡎ ⠛⢫⠙⢫⢫  ⠈⠦⠼          ⡃⡀⢸⠼⣤⡄                        ⡀⣀⣀⡐⡶⣣⢤⠖⠉",
            "   ⢀⡽⠟⠃  ⠈⠱⡀       ⠙⠢⣀⣨⠆⠈⠁⢧⡀          ⣸⣷ ⢹⣷⣼⣸⠃                       ⢀⡐⢀ ⠁⡚⣨⠆   ",
            "          ⠘⢳⡀        ⠈⠾  ⣀⣀⣽         ⠸⢼⣇⡧⠋⠉⠁                          ⠉⣿  ⠢⠂    ",
            "           ⠈⢻           ⠜⢹⣵⠻⠇         ⠈⢻  ⢀⡀  ⢠⣠⡤ ⢀⢤                  ⢰⣯        ",
            "            ⢼          ⢀⣾⠛⠉          ⠐⡖⠒⡰⢺⣞⣵⡄⢀⣏⡭⣙⡄⢕⢫⡀             ⢀ ⢠⠖⢱⡿⠃       ",
            "            ⠸         ⠠⡎             ⠰⣅⣰⣃⣘⡣⡿⢻⡿⣁⣀  ⠸⣽             ⠐⣿⣽⣫ ⡸⡇        ",
            "             ⠳⣄       ⡰⠃             ⢀⠎⠉  ⢧⡀⣠⣛⠈⢻                  ⢻⠘⢺⡿⠚⠁        ",
            "              ⢻⣇  ⣠⠲⠖⢲⡇              ⡸     ⠉⠃⠈⠉⣿  ⢰⣆              ⢸ ⠈⠁          ",
            "              ⠈⢿⣆ ⡟  ⣘⣻             ⡸          ⢸⢇ ⠈⠯⢿⡒⠲⡀   ⢀⡀    ⣀⢾             ",
            "    ⠈⢳          ⠸⡀⢳⣠⢾⠉⢹⣦⣤⣀          ⡇           ⡿⡄  ⢰⠃ ⠑⡂ ⢠⠏⢣  ⣼⡮⠁⢈⡀            ",
            "                 ⠙⠲⢆⡿⢦⠈⠉⠁⠁          ⡇           ⠱⣇⣀⠼⠃   ⡃⢰⠃ ⠸⢶ ⠘⠄ ⢾⡁            ",
            "                    ⠙⣾⣀⡴⡶⢤⣤         ⢳            ⠻⠵⡆    ⠸⣸   ⢸⡳⡤⠃⢀⡾⣿            ",
            "                     ⠘⢻⠁  ⠈⠦⣄        ⢧⣀⣀⠤⣀        ⢐⠁    ⠈⠩⠆  ⣘⣧⠁ ⡸⡔⢿            ",
            "                      ⡸     ⢨         ⠁  ⠉⡇      ⢀⠎          ⢻⢿⠄⡴⢑⣧⡠⡄           ",
            "                      ⡇     ⠈⠋⠦⡄         ⠈⡆     ⢠⠃            ⢏⡇⢧⣼⣾⣧⣽⣿⠶⢤⡀⣤      ",
            "                      ⣇        ⠈⡇         ⢸     ⢸             ⠈⠶⣦⣄⣋⣁⡀⠸⣵⢠⣻⠋⠷⣄    ",
            "                      ⠰⡀       ⣰⠁         ⢘⠆    ⢸ ⢠⡀              ⠙⠋⢠⠦⡄⣷⠙⠃ ⠙    ",
            "⠄                      ⠣⡀      ⡃          ⢸     ⣸⢡⢾⠆               ⡞⠛⠘⢧⡏⡆   ⠸⠄ ⡤",
            "                        ⠱     ⢠⠃          ⠸⡀   ⢸⠁⢸⢨              ⡤⠚     ⠱⡀  ⢦  ⠁",
            "                        ⠅    ⡖⠉            ⡇   ⡜ ⠸⠔              ⡇       ⢳      ",
            "                        ⡇   ⢀⠃             ⢱⡀ ⢰⠃                 ⣇  ⢀⡀   ⢸      ",
            "                       ⢀⠃  ⡦⠏              ⠈⠷⠖⠃                  ⠾⠴⠊⠁⠹⣦  ⡞    ⣄ ",
            "                       ⢸  ⡤⠃                                          ⠘⢲⠖⠃    ⣽⡆",
            "                       ⢸ ⣸⠁                                            ⠈⠿   ⢀⢼⠏ ",
            "                       ⠞ ⡗                             ⣄                    ⠈⠋  ",
            "                       ⢧⡼⡁⠲⠂                                                    ",
            "                        ⠙⠉                                                      ",
            "                           ⡀                                                    ",
            "                         ⣴⠏⠁                      ⣀⡤⢤⣀⣀  ⢀⣀⣤⣀⣀⡴⣄⡤⢤⣀⠤⠤⠴⣄⣀⡀       ",
            "                 ⣀⣀    ⣠⣿⡍⣆          ⣠⣤⣤⠤⠴⠶⠖⠲⠤⠔⠛⠒⠉   ⠈⠨⣇⠖⠋              ⠈⠉⠓⠢⠤⢄  ",
            "     ⡀ ⣠⠤⠴⠒⠚⠛⠛⠒⠢⠤⠿⠙⠉⠉⠑⢋⣚⣉⠥⠚      ⢀⣀⡠⠟⠁                                      ⡴⠋  ",
            "   ⠐⠶⣛⣫⡤              ⠐⢏⣀⣤⣤ ⣴⣋⢇⢀⣮⡥                                         ⣴⠓   ",
            "⠤⠤⠤⠤⡀⣈⢣⣠⡄                 ⠉⠊⠉⠉⠉                                            ⠈⠓⠆⠤⠤",
            "                                                                                ");
    BufferAssertions.assertBufferEq(buffer, expected);
  }
}
