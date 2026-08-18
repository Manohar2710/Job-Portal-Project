import { ComponentFixture, TestBed } from '@angular/core/testing';

import { LoginWithTemplateForm } from './login-with-template-form';

describe('LoginWithTemplateForm', () => {
  let component: LoginWithTemplateForm;
  let fixture: ComponentFixture<LoginWithTemplateForm>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [LoginWithTemplateForm],
    }).compileComponents();

    fixture = TestBed.createComponent(LoginWithTemplateForm);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
