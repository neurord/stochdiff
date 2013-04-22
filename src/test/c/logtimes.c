#include <stdio.h>
#include <math.h>

unsigned long long int rdtsc(void) {
    unsigned a, d;

    __asm__ volatile("rdtsc" : "=a" (a), "=d" (d));

    return ((unsigned long long)a) | (((unsigned long long)d) << 32);;
}

#define N 10000000

int main() {
    unsigned long long t0, t1;

    double a = 0;

    t0 = rdtsc();
    for(int i=1; i < N; i++)
            a += log((double) i);
    t1 = rdtsc();
    printf("log: %g → %f\n", (t1-t0) / (double)N, a);

    a = 0;
    t0 = rdtsc();
    for(int i=1; i < N; i++)
            a += log10((double) i);
    t1 = rdtsc();
    printf("log10: %g → %f\n", (t1-t0) / (double)N, a);

    a = 0;
    t0 = rdtsc();
    for(int i=1; i < N; i++)
            a += log1p((double) i);
    t1 = rdtsc();
    printf("log1p: %g → %f\n", (t1-t0) / (double)N, a);

    a = 0;
    t0 = rdtsc();
    for(int i=1; i < N; i++)
            a += log2((double) i);
    t1 = rdtsc();
    printf("log2: %g → %f\n", (t1-t0) / (double)N, a);

    a = 0;
    t0 = rdtsc();
    for(int i=1; i < N; i++)
            a += logb((double) i);
    t1 = rdtsc();
    printf("logb: %g → %f\n", (t1-t0) / (double)N, a);
}

/**

bupkis:

log: 121.694 → 151180949.369473
log10: 123.036 → 65657052.080060
log1p: 94.3514 → 151180965.487568
log2: 103.263 → 218108005.932198
logb: 13.1296 → 213222786.000000

nebish:

log: 139.317 → 151180949.369473
log10: 173.591 → 65657052.080060
log1p: 110.711 → 151180965.487568
log2: 150.551 → 218108005.932198
logb: 23.4293 → 213222786.000000

*/
