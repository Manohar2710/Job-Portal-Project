import { TestBed } from '@angular/core/testing';

import { RxJSCommonOperartors } from './rx-jscommon-operartors';

describe('RxJSCommonOperartors', () => {
  let service: RxJSCommonOperartors;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(RxJSCommonOperartors);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
