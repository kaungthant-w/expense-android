import java.util.regex.Pattern

fun main() {
    val sqlPattern = "(('|(\\-\\-)|(;)|(\\|)|(\\*)|(%)|(\\?)|(\\[)|(\\])|(\\{)|(\\}))" +
        "|(union(\\s+(all|distinct))?\\s+(select))" +
        "|(insert\\s+(into)?)" +
        "|(update\\s+\\w+\\s+set)" +
        "|(delete\\s+(from)?)" +
        "|(drop\\s+(table|database|schema|view|index))" +
        "|(alter\\s+(table|database|schema))" +
        "|(exec(ute)?\\s+)" +
        "|(script\\s*:)"
    
    println("Pattern length: ${sqlPattern.length}")
    println("Pattern: $sqlPattern")
    
    // Print character at index 259 if it exists
    if (sqlPattern.length > 259) {
        println("Character at index 259: '${sqlPattern[259]}'")
        println("Context around index 259: '${sqlPattern.substring(maxOf(0, 259-10), minOf(sqlPattern.length, 259+10))}'")
    }
    
    // Try to compile the pattern
    try {
        Pattern.compile(sqlPattern, Pattern.CASE_INSENSITIVE)
        println("Pattern compiled successfully!")
    } catch (e: Exception) {
        println("Pattern compilation failed: ${e.message}")
    }
}
