-keepattributes *Annotation*, InnerClasses, Signature
-dontwarn java.lang.invoke.**

# Room looks up its generated implementation by name (<database>_Impl), so neither the database
# class nor the generated class may be renamed. WorkManager's own database arrives through Glance.
-keep class * extends androidx.room.RoomDatabase { <init>(); }
-keep class **_Impl { <init>(...); }
-dontwarn androidx.room.paging.**

# kotlinx.serialization resolves serializers through the Companion and a generated $$serializer.
# Without these the timer state and layout silently fail to decode and reset to defaults.
-keepclassmembers class io.github.melastore.stanza.** {
	*** Companion;
}
-keepclasseswithmembers class io.github.melastore.stanza.** {
	kotlinx.serialization.KSerializer serializer(...);
}
-keep,includedescriptorclasses class io.github.melastore.stanza.**$$serializer {
	*;
}
