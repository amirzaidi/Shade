.class public abstract Lcom/google/android/libraries/gsa/launcherclient/AbsServiceStatusChecker;
.super Ljava/lang/Object;
.source "AbsServiceStatusChecker.java"


# annotations
.annotation system Ldalvik/annotation/MemberClasses;
    value = {
        Lcom/google/android/libraries/gsa/launcherclient/AbsServiceStatusChecker$StatusCallback;
    }
.end annotation


# instance fields
.field final a:Landroid/content/Context;


# direct methods
.method protected constructor <init>(Landroid/content/Context;)V
    .locals 0

    .line 1
    invoke-direct {p0}, Ljava/lang/Object;-><init>()V

    .line 2
    iput-object p1, p0, Lcom/google/android/libraries/gsa/launcherclient/AbsServiceStatusChecker;->a:Landroid/content/Context;

    return-void
.end method

.method private static a()V
    .locals 2

    .line 11
    invoke-static {}, Landroid/os/Looper;->getMainLooper()Landroid/os/Looper;

    move-result-object v0

    invoke-virtual {v0}, Landroid/os/Looper;->getThread()Ljava/lang/Thread;

    move-result-object v0

    invoke-static {}, Ljava/lang/Thread;->currentThread()Ljava/lang/Thread;

    move-result-object v1

    if-ne v0, v1, :cond_0

    return-void

    .line 12
    :cond_0
    new-instance v0, Ljava/lang/IllegalStateException;

    const-string v1, "Must be called on the main thread."

    invoke-direct {v0, v1}, Ljava/lang/IllegalStateException;-><init>(Ljava/lang/String;)V

    throw v0
.end method

.method static synthetic a(Lcom/google/android/libraries/gsa/launcherclient/AbsServiceStatusChecker;)V
    .locals 0

    .line 14
    invoke-static {}, Lcom/google/android/libraries/gsa/launcherclient/AbsServiceStatusChecker;->a()V

    return-void
.end method


# virtual methods
.method protected final a(Lcom/google/android/libraries/gsa/launcherclient/AbsServiceStatusChecker$StatusCallback;Landroid/content/Intent;)V
    .locals 3

    const-string v0, "com.google.android.googlequicksearchbox"

    .line 4
    invoke-virtual {p2, v0}, Landroid/content/Intent;->setPackage(Ljava/lang/String;)Landroid/content/Intent;

    .line 5
    new-instance v0, Lcom/google/android/libraries/gsa/launcherclient/b;

    invoke-direct {v0, p0, p1}, Lcom/google/android/libraries/gsa/launcherclient/b;-><init>(Lcom/google/android/libraries/gsa/launcherclient/AbsServiceStatusChecker;Lcom/google/android/libraries/gsa/launcherclient/AbsServiceStatusChecker$StatusCallback;)V

    .line 6
    iget-object v1, p0, Lcom/google/android/libraries/gsa/launcherclient/AbsServiceStatusChecker;->a:Landroid/content/Context;

    const/4 v2, 0x1

    invoke-virtual {v1, p2, v0, v2}, Landroid/content/Context;->bindService(Landroid/content/Intent;Landroid/content/ServiceConnection;I)Z

    move-result p2

    if-nez p2, :cond_0

    .line 8
    new-instance p2, Landroid/os/Handler;

    invoke-static {}, Landroid/os/Looper;->getMainLooper()Landroid/os/Looper;

    move-result-object v0

    invoke-direct {p2, v0}, Landroid/os/Handler;-><init>(Landroid/os/Looper;)V

    new-instance v0, Lcom/google/android/libraries/gsa/launcherclient/a;

    invoke-direct {v0, p0, p1}, Lcom/google/android/libraries/gsa/launcherclient/a;-><init>(Lcom/google/android/libraries/gsa/launcherclient/AbsServiceStatusChecker;Lcom/google/android/libraries/gsa/launcherclient/AbsServiceStatusChecker$StatusCallback;)V

    .line 9
    invoke-virtual {p2, v0}, Landroid/os/Handler;->post(Ljava/lang/Runnable;)Z

    :cond_0
    return-void
.end method

.method protected abstract a(Landroid/os/IBinder;)Z
    .annotation system Ldalvik/annotation/Throws;
        value = {
            Landroid/os/RemoteException;
        }
    .end annotation
.end method
