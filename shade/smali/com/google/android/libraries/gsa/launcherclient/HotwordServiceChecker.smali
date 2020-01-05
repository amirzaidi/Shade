.class public Lcom/google/android/libraries/gsa/launcherclient/HotwordServiceChecker;
.super Lcom/google/android/libraries/gsa/launcherclient/AbsServiceStatusChecker;
.source "HotwordServiceChecker.java"


# direct methods
.method public constructor <init>(Landroid/content/Context;)V
    .locals 0

    .line 1
    invoke-direct {p0, p1}, Lcom/google/android/libraries/gsa/launcherclient/AbsServiceStatusChecker;-><init>(Landroid/content/Context;)V

    return-void
.end method


# virtual methods
.method protected final a(Landroid/os/IBinder;)Z
    .locals 0
    .annotation system Ldalvik/annotation/Throws;
        value = {
            Landroid/os/RemoteException;
        }
    .end annotation

    .line 5
    invoke-static {p1}, Lcom/google/android/libraries/a/b;->a(Landroid/os/IBinder;)Lcom/google/android/libraries/a/a;

    move-result-object p0

    invoke-interface {p0}, Lcom/google/android/libraries/a/a;->e()Z

    move-result p0

    return p0
.end method

.method public checkHotwordService(Lcom/google/android/libraries/gsa/launcherclient/AbsServiceStatusChecker$StatusCallback;)V
    .locals 1

    .line 3
    iget-object v0, p0, Lcom/google/android/libraries/gsa/launcherclient/HotwordServiceChecker;->a:Landroid/content/Context;

    invoke-static {v0}, Lcom/google/android/libraries/gsa/launcherclient/LauncherClient;->a(Landroid/content/Context;)Landroid/content/Intent;

    move-result-object v0

    invoke-virtual {p0, p1, v0}, Lcom/google/android/libraries/gsa/launcherclient/HotwordServiceChecker;->a(Lcom/google/android/libraries/gsa/launcherclient/AbsServiceStatusChecker$StatusCallback;Landroid/content/Intent;)V

    return-void
.end method
