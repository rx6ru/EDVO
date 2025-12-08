# ProGuard rules for Edvo

# Strip logging in Release builds
-assumenosideeffects class java.io.PrintStream {
    void println(java.lang.Object);
    void println(java.lang.String);
    void println();
    void println(boolean);
    void println(int);
    void println(long);
    void println(float);
    void println(double);
}

-assumenosideeffects class android.util.Log {
    public static boolean isLoggable(java.lang.String, int);
    public static int v(...);
    public static int i(...);
    public static int w(...);
    public static int d(...);
    public static int e(...);
}