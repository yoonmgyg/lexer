import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.ufl.cise.plc.Token;

class Lexer {
  private final String source;
  private final List<Token> tokens = new ArrayList<>();
  
  Lexer(String source) {
    this.source = source;
  }
}
