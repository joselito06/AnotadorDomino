# =========================================================
# REGLAS DE PROGUARD / R8 PARA ANOTADOR DOMINÓ
# =========================================================

# 1. MANTENER LÍNEAS PARA FIREBASE CRASHLYTICS (¡CRÍTICO!)
# Esto permite que los reportes de errores en Firebase muestren el archivo y la línea exacta del código, y no letras ofuscadas.
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# 2. REGLAS PARA ROOM DATABASE
# Evita que R8 elimine o rompa las entidades de la base de datos y la clase principal.
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class * { *; }
-keep @androidx.room.Dao interface * { *; }

# 3. REGLAS PARA ADMOB (GOOGLE MOBILE ADS)
# Necesario para que los anuncios Banner, Intersticiales y Nativos carguen correctamente.
-keep public class com.google.android.gms.ads.** {
   public *;
}
-keep public class com.google.ads.** {
   public *;
}

# 4. REGLAS PARA HILT / DAGGER
# Aunque el plugin de Hilt hace casi todo el trabajo, estas reglas evitan problemas con componentes generados.
-keep,allowobfuscation,allowshrinking interface dagger.hilt.internal.GeneratedComponent
-keep class * extends dagger.hilt.internal.GeneratedComponent

# 5. REGLAS PARA KOTLIN COROUTINES
# Evita problemas de reflexión interna en las corrutinas.
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
-keepclassmembernames class kotlinx.** {
    volatile <fields>;
}

# 6. MODELOS DE DATOS (Opcional pero recomendado)
# Si en algún momento usas Gson o Moshi para leer/escribir datos (por ejemplo, desde DataStore o Firebase),
# descomenta esta línea y pon la ruta de tu carpeta de modelos:
# -keep class com.jbncode.anotadordomino.domain.model.** { *; }