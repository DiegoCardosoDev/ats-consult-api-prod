import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { provideRouter } from '@angular/router';

import { JobPositionFormComponent } from './job-position-form.component';

describe('JobPositionFormComponent', () => {
  let component: JobPositionFormComponent;
  let fixture: ComponentFixture<JobPositionFormComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [JobPositionFormComponent],
      providers: [
        provideHttpClient(),
        provideHttpClientTesting(),
        provideRouter([])
      ]
    })
    .compileComponents();
    
    fixture = TestBed.createComponent(JobPositionFormComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
