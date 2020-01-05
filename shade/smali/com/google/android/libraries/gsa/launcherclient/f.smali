.class final Lcom/google/android/libraries/gsa/launcherclient/f;
.super Landroid/content/BroadcastReceiver;
.source "LauncherClient.java"


# instance fields
.field private final synthetic a:Lcom/google/android/libraries/gsa/launcherclient/LauncherClient;


# direct methods
.method constructor <init>(Lcom/google/android/libraries/gsa/launcherclient/LauncherClient;)V
    .locals 0

    .line 1
    iput-object p1, p0, Lcom/google/android/libraries/gsa/launcherclient/f;->a:Lcom/google/android/libraries/gsa/launcherclient/LauncherClient;

    invoke-direct {p0}, Landroid/content/BroadcastReceiver;-><init>()V

    return-void
.end method


# virtual methods
.method public final onReceive(Landroid/content/Context;Landroid/content/Intent;)V
    .locals 2

    .line 2
    invoke-virtual {p2}, Landroid/content/Intent;->getData()Landroid/net/Uri;

    move-result-object p2

    .line 3
    sget v0, Landroid/os/Build$VERSION;->SDK_INT:I

    const/16 v1, 0x13

    if-ge v0, v1, :cond_0

    if-eqz p2, :cond_1

    .line 4
    invoke-virtual {p2}, Landroid/net/Uri;->getSchemeSpecificPart()Ljava/lang/String;

    move-result-object p2

    const-string v0, "com.google.android.googlequicksearchbox"

    invoke-virtual {v0, p2}, Ljava/lang/String;->equals(Ljava/lang/Object;)Z

    move-result p2

    if-eqz p2, :cond_1

    .line 5
    :cond_0
    iget-object p2, p0, Lcom/google/android/libraries/gsa/launcherclient/f;->a:Lcom/google/android/libraries/gsa/launcherclient/LauncherClient;

    invoke-static {p2}, Lcom/google/android/libraries/gsa/launcherclient/LauncherClient;->a(Lcom/google/android/libraries/gsa/launcherclient/LauncherClient;)Lcom/google/android/libraries/gsa/launcherclient/i;

    move-result-object p2

    invoke-virtual {p2}, Lcom/google/android/libraries/gsa/launcherclient/i;->a()V

    .line 6
    iget-object p2, p0, Lcom/google/android/libraries/gsa/launcherclient/f;->a:Lcom/google/android/libraries/gsa/launcherclient/LauncherClient;

    invoke-static {p2}, Lcom/google/android/libraries/gsa/launcherclient/LauncherClient;->b(Lcom/google/android/libraries/gsa/launcherclient/LauncherClient;)Lcom/google/android/libraries/gsa/launcherclient/c;

    move-result-object p2

    invoke-virtual {p2}, Lcom/google/android/libraries/gsa/launcherclient/c;->a()V

    .line 7
    invoke-static {p1}, Lcom/google/android/libraries/gsa/launcherclient/LauncherClient;->b(Landroid/content/Context;)V

    .line 8
    iget-object p1, p0, Lcom/google/android/libraries/gsa/launcherclient/f;->a:Lcom/google/android/libraries/gsa/launcherclient/LauncherClient;

    invoke-static {p1}, Lcom/google/android/libraries/gsa/launcherclient/LauncherClient;->c(Lcom/google/android/libraries/gsa/launcherclient/LauncherClient;)I

    move-result p1

    and-int/lit8 p1, p1, 0x2

    if-eqz p1, :cond_1

    .line 9
    iget-object p0, p0, Lcom/google/android/libraries/gsa/launcherclient/f;->a:Lcom/google/android/libraries/gsa/launcherclient/LauncherClient;

    invoke-virtual {p0}, Lcom/google/android/libraries/gsa/launcherclient/LauncherClient;->reconnect()V

    :cond_1
    return-void
.end method
