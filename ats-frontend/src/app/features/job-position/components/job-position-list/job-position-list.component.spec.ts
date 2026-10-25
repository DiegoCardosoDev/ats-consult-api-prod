import { ComponentFixture, TestBed } from '@angular/core/testing';

import { JobPositionListComponent } from './job-position-list.component';

describe('JobPositionListComponent', () => {
  let component: JobPositionListComponent;
  let fixture: ComponentFixture<JobPositionListComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [JobPositionListComponent]
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
