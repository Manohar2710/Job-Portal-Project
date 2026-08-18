import { TestBed } from '@angular/core/testing';

import { RxJSSubjectTypes } from './rx-jssubject-types';

describe('RxJSSubjectTypes', () => {
  let service: RxJSSubjectTypes;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(RxJSSubjectTypes);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
