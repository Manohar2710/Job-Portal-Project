import { TestBed } from '@angular/core/testing';

import { UserStoreServiceWithBS } from './user-store-service-with-bs';

describe('UserStoreServiceWithBS', () => {
  let service: UserStoreServiceWithBS;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(UserStoreServiceWithBS);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
