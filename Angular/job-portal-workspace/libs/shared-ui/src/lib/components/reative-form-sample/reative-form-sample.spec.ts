import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ReativeFormSample } from './reative-form-sample';

describe('ReativeFormSample', () => {
  let component: ReativeFormSample;
  let fixture: ComponentFixture<ReativeFormSample>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ReativeFormSample],
    }).compileComponents();

    fixture = TestBed.createComponent(ReativeFormSample);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
