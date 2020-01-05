.class final Lcom/google/android/libraries/gsa/launcherclient/b;
.super Ljava/lang/Object;
.source "AbsServiceStatusChecker.java"

# interfaces
.implements Landroid/content/ServiceConnection;


# instance fields
.field private a:Lcom/google/android/libraries/gsa/launcherclient/AbsServiceStatusChecker$StatusCallback;

.field private final synthetic b:Lcom/google/android/libraries/gsa/launcherclient/AbsServiceStatusChecker;


# direct methods
.method public constructor <init>(Lcom/google/android/libraries/gsa/launcherclient/AbsServiceStatusChecker;Lcom/google/android/libraries/gsa/launcherclient/AbsServiceStatusChecker$StatusCallback;)V
    .locals 0

    .line 1
    iput-object p1, p0, Lcom/google/android/libraries/gsa/launcherclient/b;->b:Lcom/google/android/libraries/gsa/launcherclient/AbsServiceStatusChecker;

    invoke-direct {p0}, Ljava/lang/Object;-><init>()V

    .line 2
    iput-object p2, p0, Lcom/google/android/libraries/gsa/launcherclient/b;->a:Lcom/google/android/libraries/gsa/launcherclient/AbsServiceStatusChecker$StatusCallback;

    return-void
.end method


# virtual methods
.method public final onServiceConnected(Landroid/content/ComponentName;Landroid/os/IBinder;)V
    .locals 1

    .line 5
    :try_start_0
    iget-object p1, p0, Lcom/google/android/libraries/gsa/launcherclient/b;->a:Lcom/google/android/libraries/gsa/launcherclient/AbsServiceStatusChecker$StatusCallback;

    iget-object v0, p0, Lcom/google/android/libraries/gsa/launcherclient/b;->b:Lcom/google/android/libraries/gsa/launcherclient/AbsServiceStatusChecker;

    invoke-virtual {v0, p2}, Lcom/google/android/libraries/gsa/launcherclient/AbsServiceStatusChecker;->a(Landroid/os/IBinder;)Z

    move-result p2

    invoke-interface {p1, p2}, Lcom/google/android/libraries/gsa/launcherclient/AbsServiceStatusChecker$StatusCallback;->isRunning(Z)V
    :try_end_0
    .catch Landroid/os/RemoteException; {:try_start_0 .. :try_end_0} :catch_0
    .catchall {:try_start_0 .. :try_end_0} :catchall_0

    .line 6
    iget-object p1, p0, Lcom/google/android/libraries/gsa/launcherclient/b;->b:Lcom/google/android/libraries/gsa/launcherclient/AbsServiceStatusChecker;

    iget-object p1, p1, Lcom/google/android/libraries/gsa/launcherclient/AbsServiceStatusChecker;->a:Landroid/content/Context;

    invoke-virtual {p1, p0}, Landroid/content/Context;->unbindService(Landroid/content/ServiceConnection;)V

    return-void

    :catchall_0
    move-exception p1

    goto :goto_0

    :catch_0
    move-exception p1

    :try_start_1
    const-string p2, "AbsServiceStatusChecker"

    const-string v0, "isServiceRunning - remote call failed"

    .line 9
    invoke-static {p2, v0, p1}, Landroid/util/Log;->w(Ljava/lang/String;Ljava/lang/String;Ljava/lang/Throwable;)I
    :try_end_1
    .catchall {:try_start_1 .. :try_end_1} :catchall_0

    .line 10
    iget-object p1, p0, Lcom/google/android/libraries/gsa/launcherclient/b;->b:Lcom/google/android/libraries/gsa/launcherclient/AbsServiceStatusChecker;

    iget-object p1, p1, Lcom/google/android/libraries/gsa/launcherclient/AbsServiceStatusChecker;->a:Landroid/content/Context;

    invoke-virtual {p1, p0}, Landroid/content/Context;->unbindService(Landroid/content/ServiceConnection;)V

    .line 13
    iget-object p0, p0, Lcom/google/android/libraries/gsa/launcherclient/b;->a:Lcom/google/android/libraries/gsa/launcherclient/AbsServiceStatusChecker$StatusCallback;

    const/4 p1, 0x0

    invoke-interface {p0, p1}, Lcom/google/android/libraries/gsa/launcherclient/AbsServiceStatusChecker$StatusCallback;->isRunning(Z)V

    return-void

    .line 12
    :goto_0
    iget-object p2, p0, Lcom/google/android/libraries/gsa/launcherclient/b;->b:Lcom/google/android/libraries/gsa/launcherclient/AbsServiceStatusChecker;

    iget-object p2, p2, Lcom/google/android/libraries/gsa/launcherclient/AbsServiceStatusChecker;->a:Landroid/content/Context;

    invoke-virtual {p2, p0}, Landroid/content/Context;->unbindService(Landroid/content/ServiceConnection;)V

    throw p1
.end method

.method public final onServiceDisconnected(Landroid/content/ComponentName;)V
    .locals 0

    return-void
.end method
