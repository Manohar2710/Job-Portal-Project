import { Injectable } from '@angular/core';
import { AsyncSubject, BehaviorSubject, concatMap, delay, exhaustMap, interval, mergeMap, of, ReplaySubject, Subject, switchMap, take } from 'rxjs';

@Injectable({
  providedIn: 'root',
})
export class RxJSSubjectTypes {

  constructor() {
    console.log("Subject")
    const subject$ = new Subject<number>();

    subject$.next(1);

    subject$.subscribe((val) => console.log('Sub A : '+ val));
    subject$.next(2);

    subject$.subscribe((val) => console.log('Sub B : '+val))
    subject$.next(3);

    console.log("Behavioral Subject");

    // required initial value
    const behaviorSubject$ = new BehaviorSubject<string>("Logged Out");

    behaviorSubject$.subscribe((val) => console.log("Sub A" + val)); // triggered immediately with current vlaue

    behaviorSubject$.next('Logged In');

    behaviorSubject$.subscribe((val) => console.log("Sub B : "+ val )); // Sub B subscibe after 
    // state change and gets latest state


    console.log("Replay Subject");
    const replaySubject$ = new ReplaySubject<number>(2);
    replaySubject$.next(10);
    replaySubject$.next(20);
    replaySubject$.next(30);
    replaySubject$.subscribe((val) => console.log("Sub A : "+ val))


    console.log("Async Subject");
    // emits the last event fired and only executed after the .complete() method is invoked

    const asyncSubject$ = new AsyncSubject<string>();

    asyncSubject$.next("Step 1");
    asyncSubject$.next("Step 2");
    asyncSubject$.next("Final Result");

    asyncSubject$.subscribe((val) => console.log("Sub A : "+ val));

    asyncSubject$.complete();

    asyncSubject$.subscribe((val) => console.log("Sub B :"+ val));


    console.log("Higher order mapping operator");
    console.log("switch map");

    const getData = (id: string) => of(`Result ${id}`).pipe(delay(1000));
    const trigger$ = interval(300).pipe(take(3));
    // switch map
    trigger$.pipe(
      switchMap(id => getData(`switch : ${id}`))
    ).subscribe(console.log); // emits 2

    console.log("Concat map")
    // concat map
    trigger$.pipe(
      concatMap(id => getData(`concat : ${id}`))
    ).subscribe(console.log)

    // merge map
    console.log("merge map")
    trigger$.pipe(
      mergeMap(id => getData(`mergeMap : ${id}`))
    ).subscribe(console.log)

    // exhastMap
    console.log("exhaust map");
    trigger$.pipe(
      exhaustMap(id => getData(`ExhaustMap : ${id}`))
    ).subscribe(console.log)
  }


}
