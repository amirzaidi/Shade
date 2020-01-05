.class final Lcom/google/android/libraries/gsa/launcherclient/a;
.super Ljava/lang/Object;
.source "AbsServiceStatusChecker.java"

# interfaces
.implements Ljava/lang/Runnable;


# instance fields
.field private final synthetic a:Lcom/google/android/libraries/gsa/launcherclient/AbsServiceStatusChecker$StatusCallback;

.field private final synthetic b:Lcom/google/android/libraries/gsa/launcherclient/AbsServiceStatusChecker;


# direct methods
.method constructor <init>(Lcom/google/android/libraries/gsa/launcherclient/AbsServiceStatusChecker;Lcom/google/android/libraries/gsa/launcherclient/AbsServiceStatusChecker$StatusCallback;)V
    .locals 0

    .line 1
    iput-object p1, p0, Lcom/google/android/libraries/gsa/launcherclient/a;->b:Lcom/google/android/libraries/gsa/launcherclient/AbsServiceStatusChecker;

    iput-object p2, p0, Lcom/google/android/libraries/gsa/launcherclient/a;->a:Lcom/google/android/libraries/gsa/launcherclient/AbsServiceStatusChecker$StatusCallback;

    invoke-direct {p0}, Ljava/lang/Object;-><init>()V

    return-void
.end method


# virtual methods
.method public final run()V
    .locals 1

    .line 2
    iget-object v0, p0, Lcom/google/android/libraries/gsa/launcherclient/a;->b:Lcom/google/android/libraries/gsa/launcherclient/AbsServiceStatusChecker;

    invoke-static {v0}, Lcom/google/android/libraries/gsa/launcherclient/AbsServiceStatusChecker;->a(Lcom/google/android/libraries/gsa/launcherclient/AbsServiceStatusChecker;)V

    .line 3
    iget-object p0, p0, Lcom/google/android/libraries/gsa/launcherclient/a;->a:Lcom/google/android/libraries/gsa/launcherclient/AbsServiceStatusChecker$StatusCallback;

    const/4 v0, 0x0

    invoke-interface {p0, v0}, Lcom/google/android/libraries/gsa/launcherclient/AbsServiceStatusChecker$StatusCallback;->isRunning(Z)V

    return-void
.end method
