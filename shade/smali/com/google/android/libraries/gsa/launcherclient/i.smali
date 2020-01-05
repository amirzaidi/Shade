.class Lcom/google/android/libraries/gsa/launcherclient/i;
.super Ljava/lang/Object;
.source "SimpleServiceConnection.java"

# interfaces
.implements Landroid/content/ServiceConnection;


# instance fields
.field private final a:Landroid/content/Context;

.field private final b:I

.field private c:Z


# direct methods
.method constructor <init>(Landroid/content/Context;I)V
    .locals 0

    .line 1
    invoke-direct {p0}, Ljava/lang/Object;-><init>()V

    .line 2
    iput-object p1, p0, Lcom/google/android/libraries/gsa/launcherclient/i;->a:Landroid/content/Context;

    .line 3
    iput p2, p0, Lcom/google/android/libraries/gsa/launcherclient/i;->b:I

    return-void
.end method


# virtual methods
.method public final a()V
    .locals 1

    .line 7
    iget-boolean v0, p0, Lcom/google/android/libraries/gsa/launcherclient/i;->c:Z

    if-eqz v0, :cond_0

    .line 8
    iget-object v0, p0, Lcom/google/android/libraries/gsa/launcherclient/i;->a:Landroid/content/Context;

    invoke-virtual {v0, p0}, Landroid/content/Context;->unbindService(Landroid/content/ServiceConnection;)V

    const/4 v0, 0x0

    .line 9
    iput-boolean v0, p0, Lcom/google/android/libraries/gsa/launcherclient/i;->c:Z

    :cond_0
    return-void
.end method

.method public final b()Z
    .locals 0

    .line 11
    iget-boolean p0, p0, Lcom/google/android/libraries/gsa/launcherclient/i;->c:Z

    return p0
.end method

.method public final c()Z
    .locals 3

    .line 12
    iget-boolean v0, p0, Lcom/google/android/libraries/gsa/launcherclient/i;->c:Z

    if-nez v0, :cond_0

    .line 13
    :try_start_0
    iget-object v0, p0, Lcom/google/android/libraries/gsa/launcherclient/i;->a:Landroid/content/Context;

    iget-object v1, p0, Lcom/google/android/libraries/gsa/launcherclient/i;->a:Landroid/content/Context;

    .line 14
    invoke-static {v1}, Lcom/google/android/libraries/gsa/launcherclient/LauncherClient;->a(Landroid/content/Context;)Landroid/content/Intent;

    move-result-object v1

    iget v2, p0, Lcom/google/android/libraries/gsa/launcherclient/i;->b:I

    invoke-virtual {v0, v1, p0, v2}, Landroid/content/Context;->bindService(Landroid/content/Intent;Landroid/content/ServiceConnection;I)Z

    move-result v0

    iput-boolean v0, p0, Lcom/google/android/libraries/gsa/launcherclient/i;->c:Z
    :try_end_0
    .catch Ljava/lang/SecurityException; {:try_start_0 .. :try_end_0} :catch_0

    goto :goto_0

    :catch_0
    move-exception v0

    const-string v1, "LauncherClient"

    const-string v2, "Unable to connect to overlay service"

    .line 17
    invoke-static {v1, v2, v0}, Landroid/util/Log;->e(Ljava/lang/String;Ljava/lang/String;Ljava/lang/Throwable;)I

    .line 18
    :cond_0
    :goto_0
    iget-boolean p0, p0, Lcom/google/android/libraries/gsa/launcherclient/i;->c:Z

    return p0
.end method

.method public onServiceConnected(Landroid/content/ComponentName;Landroid/os/IBinder;)V
    .locals 0

    return-void
.end method

.method public onServiceDisconnected(Landroid/content/ComponentName;)V
    .locals 0

    return-void
.end method
