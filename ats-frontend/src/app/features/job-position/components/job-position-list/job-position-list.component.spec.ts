import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { provideRouter } from '@angular/router';

import { JobPositionListComponent } from './job-position-list.component';

describe('JobPositionListComponent', () => {
  let component: JobPositionListComponent;
  let fixture: ComponentFixture<JobPositionListComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [JobPositionListComponent],
      providers: [
        provideHttpClient(),
        provideHttpClientTesting(),
        provideRouter([])
      ]
    })
    .compileComponents();
    
    fixture = TestBed.createComponent(JobPositionListComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
