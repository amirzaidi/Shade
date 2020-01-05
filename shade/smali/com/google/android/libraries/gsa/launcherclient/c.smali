.class final Lcom/google/android/libraries/gsa/launcherclient/c;
.super Lcom/google/android/libraries/gsa/launcherclient/i;
.source "AppServiceConnection.java"


# static fields
.field private static a:Lcom/google/android/libraries/gsa/launcherclient/c;


# instance fields
.field private b:Lcom/google/android/libraries/a/a;

.field private c:Ljava/lang/ref/WeakReference;
    .annotation system Ldalvik/annotation/Signature;
        value = {
            "Ljava/lang/ref/WeakReference<",
            "Lcom/google/android/libraries/gsa/launcherclient/LauncherClient;",
            ">;"
        }
    .end annotation
.end field

.field private d:Z


# direct methods
.method private constructor <init>(Landroid/content/Context;)V
    .locals 1

    const/16 v0, 0x21

    .line 4
    invoke-direct {p0, p1, v0}, Lcom/google/android/libraries/gsa/launcherclient/i;-><init>(Landroid/content/Context;I)V

    return-void
.end method

.method static a(Landroid/content/Context;)Lcom/google/android/libraries/gsa/launcherclient/c;
    .locals 1

    .line 1
    sget-object v0, Lcom/google/android/libraries/gsa/launcherclient/c;->a:Lcom/google/android/libraries/gsa/launcherclient/c;

    if-nez v0, :cond_0

    .line 2
    new-instance v0, Lcom/google/android/libraries/gsa/launcherclient/c;

    invoke-virtual {p0}, Landroid/content/Context;->getApplicationContext()Landroid/content/Context;

    move-result-object p0

    invoke-direct {v0, p0}, Lcom/google/android/libraries/gsa/launcherclient/c;-><init>(Landroid/content/Context;)V

    sput-object v0, Lcom/google/android/libraries/gsa/launcherclient/c;->a:Lcom/google/android/libraries/gsa/launcherclient/c;

    .line 3
    :cond_0
    sget-object p0, Lcom/google/android/libraries/gsa/launcherclient/c;->a:Lcom/google/android/libraries/gsa/launcherclient/c;

    return-object p0
.end method

.method private final a(Lcom/google/android/libraries/a/a;)V
    .locals 0

    .line 27
    iput-object p1, p0, Lcom/google/android/libraries/gsa/launcherclient/c;->b:Lcom/google/android/libraries/a/a;

    .line 28
    invoke-direct {p0}, Lcom/google/android/libraries/gsa/launcherclient/c;->e()Lcom/google/android/libraries/gsa/launcherclient/LauncherClient;

    move-result-object p1

    if-eqz p1, :cond_0

    .line 30
    iget-object p0, p0, Lcom/google/android/libraries/gsa/launcherclient/c;->b:Lcom/google/android/libraries/a/a;

    invoke-virtual {p1, p0}, Lcom/google/android/libraries/gsa/launcherclient/LauncherClient;->a(Lcom/google/android/libraries/a/a;)V

    :cond_0
    return-void
.end method

.method private final d()V
    .locals 1

    .line 24
    iget-boolean v0, p0, Lcom/google/android/libraries/gsa/launcherclient/c;->d:Z

    if-eqz v0, :cond_0

    iget-object v0, p0, Lcom/google/android/libraries/gsa/launcherclient/c;->b:Lcom/google/android/libraries/a/a;

    if-nez v0, :cond_0

    .line 25
    invoke-virtual {p0}, Lcom/google/android/libraries/gsa/launcherclient/c;->a()V

    :cond_0
    return-void
.end method

.method private final e()Lcom/google/android/libraries/gsa/launcherclient/LauncherClient;
    .locals 0

    .line 32
    iget-object p0, p0, Lcom/google/android/libraries/gsa/launcherclient/c;->c:Ljava/lang/ref/WeakReference;

    if-eqz p0, :cond_0

    invoke-virtual {p0}, Ljava/lang/ref/WeakReference;->get()Ljava/lang/Object;

    move-result-object p0

    check-cast p0, Lcom/google/android/libraries/gsa/launcherclient/LauncherClient;

    return-object p0

    :cond_0
    const/4 p0, 0x0

    return-object p0
.end method


# virtual methods
.method public final a(Lcom/google/android/libraries/gsa/launcherclient/LauncherClient;)Lcom/google/android/libraries/a/a;
    .locals 1

    .line 6
    new-instance v0, Ljava/lang/ref/WeakReference;

    invoke-direct {v0, p1}, Ljava/lang/ref/WeakReference;-><init>(Ljava/lang/Object;)V

    iput-object v0, p0, Lcom/google/android/libraries/gsa/launcherclient/c;->c:Ljava/lang/ref/WeakReference;

    .line 7
    iget-object p0, p0, Lcom/google/android/libraries/gsa/launcherclient/c;->b:Lcom/google/android/libraries/a/a;

    return-object p0
.end method

.method public final a(Lcom/google/android/libraries/gsa/launcherclient/LauncherClient;Z)V
    .locals 1

    .line 11
    invoke-direct {p0}, Lcom/google/android/libraries/gsa/launcherclient/c;->e()Lcom/google/android/libraries/gsa/launcherclient/LauncherClient;

    move-result-object v0

    if-eqz v0, :cond_0

    .line 12
    invoke-virtual {v0, p1}, Ljava/lang/Object;->equals(Ljava/lang/Object;)Z

    move-result p1

    if-eqz p1, :cond_0

    const/4 p1, 0x0

    .line 13
    iput-object p1, p0, Lcom/google/android/libraries/gsa/launcherclient/c;->c:Ljava/lang/ref/WeakReference;

    if-eqz p2, :cond_0

    .line 15
    invoke-virtual {p0}, Lcom/google/android/libraries/gsa/launcherclient/c;->a()V

    .line 16
    sget-object p2, Lcom/google/android/libraries/gsa/launcherclient/c;->a:Lcom/google/android/libraries/gsa/launcherclient/c;

    if-ne p2, p0, :cond_0

    .line 17
    sput-object p1, Lcom/google/android/libraries/gsa/launcherclient/c;->a:Lcom/google/android/libraries/gsa/launcherclient/c;

    :cond_0
    return-void
.end method

.method public final a(Z)V
    .locals 0

    .line 8
    iput-boolean p1, p0, Lcom/google/android/libraries/gsa/launcherclient/c;->d:Z

    .line 9
    invoke-direct {p0}, Lcom/google/android/libraries/gsa/launcherclient/c;->d()V

    return-void
.end method

.method public final onServiceConnected(Landroid/content/ComponentName;Landroid/os/IBinder;)V
    .locals 0

    .line 19
    invoke-static {p2}, Lcom/google/android/libraries/a/b;->a(Landroid/os/IBinder;)Lcom/google/android/libraries/a/a;

    move-result-object p1

    invoke-direct {p0, p1}, Lcom/google/android/libraries/gsa/launcherclient/c;->a(Lcom/google/android/libraries/a/a;)V

    return-void
.end method

.method public final onServiceDisconnected(Landroid/content/ComponentName;)V
    .locals 0

    const/4 p1, 0x0

    .line 21
    invoke-direct {p0, p1}, Lcom/google/android/libraries/gsa/launcherclient/c;->a(Lcom/google/android/libraries/a/a;)V

    .line 22
    invoke-direct {p0}, Lcom/google/android/libraries/gsa/launcherclient/c;->d()V

    return-void
.end method
