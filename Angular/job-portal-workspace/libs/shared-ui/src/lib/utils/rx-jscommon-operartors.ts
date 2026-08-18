import { Injectable } from '@angular/core';
import { BehaviorSubject, combineLatest, debounce, debounceTime, distinctUntilChanged, filter, forkJoin, interval, map, of, Subject, take, takeUntil, tap, zip } from 'rxjs';
@Injectable({
  providedIn: 'root',
})
export class RxJSCommonOperartors {

  constructor() {
    console.log("dsdsd")
    console.log("transforming and filtering")
    console.log("map")

    //1. transforming and filtering
    // map
    of(1, 2, 3, 4).pipe(
      map(val => val * 2)
    ).subscribe(console.log)

    console.log("filter")

    // filter
    of(1, 3, 4, 5, 6).pipe(
      filter(val => val % 2 != 0)
    ).subscribe(console.log);

    console.log("Utility and timming")
    console.log("Tap")

    // Utility and timming
    // Tap
    of(1, 2, 4, 5, 6).pipe(
      tap((val) => console.log(val)),
      map(val => val*3)
    ).subscribe(console.log)


    console.log("debounce")

    const searchInput$ = new Subject<string>();

    searchInput$.pipe(
      debounceTime(300) // waits for 300ms before emittig new values
    ).subscribe(console.log)

    searchInput$.next("a");
    searchInput$.next('ab');
    searchInput$.next("abc"); // output will be abc, as the rest of the request will be ignored  


    console.log("distinctUnitilChanged")
    // distinct until changed. the consecutive 
    // duplicated will be ignored and only one value will be used
    of(1,2, 1, 2, 3, 3, 4, 4).pipe(
      distinctUntilChanged()
    ).subscribe(console.log);

    console.log("Stream and unsubscribing")
    console.log("take(2)")

    // take 
    interval(1000) // emits 0, 1, 2, 3 every second 
    .pipe(take(2))
    .subscribe(console.log);

    console.log("takeUntil")
    const stop$ = new Subject<void>();
    interval(1000)
    .pipe(takeUntil(stop$))
    .subscribe(console.log)
    
    stop$.next();

    console.log("Combination Operator")
    console.log("combinelatest")
    const firstName$ = new BehaviorSubject<string>("Manohar");
    const lastName$ = new BehaviorSubject<string>('A B');

    combineLatest([firstName$, lastName$])
      .subscribe(([firstName, lastName]) => console.log(`${firstName} ${lastName}`)) // immidiatly emits Manohar A B

    lastName$.next("B"); // again emits Manohar B


    console.log("fork join")
    const user$ = of({name: "Manohar", age: 29});
    const post$ = of(['post1', 'post2']);
    forkJoin([user$, post$]).subscribe(([user, post]) => console.log(`user: ${JSON.stringify(user)} post : ${post}`))


    console.log("zip")
    const userInfoLabel$ = of(["firstName", "lastName", "age" ]);
    const userInfo$ = of(["Manohar", "B", 29]);
    zip([userInfoLabel$, userInfo$]).subscribe(
      ([userLable, userInfo]) => {console.log(`${userLable} ${userInfo}`)}
    )
  }
}
