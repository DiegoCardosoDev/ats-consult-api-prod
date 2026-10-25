import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router'; // 👈 Adicionado ActivatedRoute

import { JobPositionService } from '../../services/job-position.service';
import { TechnologyService } from '../../../technology/services/technology.service';
import { TechnologyModel } from '../../../technology/models/technology.model';
import { ClientModel } from '../../../client/models/client.model';
import { ClientService } from '../../../client/services/client.service';
import { AuthService } from '../../../../services/auth.service';
import { extractApiErrorMessage } from '../../../../core/utils/api-error.util';

@Component({
  selector: 'app-job-position-form',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterLink],
  templateUrl: './job-position-form.component.html',
  styleUrl: './job-position-form.component.scss'
})
export class JobPositionFormComponent implements OnInit {
  private fb = inject(FormBuilder);
  private jobPositionService = inject(JobPositionService);
  private technologyService = inject(TechnologyService);
  private clientService = inject(ClientService);
  private router = inject(Router);
  private route = inject(ActivatedRoute); // 👈 Injetado para ler o ID da rota
  private authService = inject(AuthService);

  jobForm!: FormGroup;
  technologies: TechnologyModel[] = [];
  clients: ClientModel[] = [];
  selectedTechIds: number[] = [];
  isLoading = false;
  errorMessage = '';
  successMessage = '';
  
  // 🟢 Novas propriedades para controlar a edição
  isEditMode = false;
  jobId?: number;

  ngOnInit(): void {
    this.initForm();
    this.loadTechnologies();
    this.loadClients();

    // 🟢 Verifica se estamos na rota de edição (ex: jobs/edit/5)
    const idParam = this.route.snapshot.paramMap.get('id');
    if (idParam) {
      this.isEditMode = true;
      this.jobId = +idParam;
      this.loadJobData(this.jobId);
    }
  }

  private initForm(): void {
    this.jobForm = this.fb.group({
      clientId: [null as number | null, [Validators.required]],
      title: ['', [Validators.required, Validators.minLength(3)]],
      description: [''],
      status: ['OPEN', [Validators.required]],
      workModel: ['HYBRID', [Validators.required]],
      contractType: ['CLT', [Validators.required]],
      educationLevel: [null],
      englishLevel: [null],
      minSalary: [null],
      maxSalary: [null],
      location: this.fb.group({
        city: [''],
        state: [''],
        country: ['']
      })
    });
  }

  private loadClients(): void {
    this.clientService.findAll().subscribe({
      next: (clients) => {
        this.clients = clients;
      },
      error: (err) => {
        this.errorMessage = extractApiErrorMessage(err, 'Não foi possível carregar os clientes.');
      }
    });
  }

  private loadTechnologies(): void {
    this.technologyService.findAll().subscribe({
      next: (data) => {
        this.technologies = data;
      },
      error: (err) => {
        console.error('Erro ao carregar tecnologias:', err);
      }
    });
  }

  // 🟢 Método para carregar os dados da vaga ao editar
  private loadJobData(id: number): void {
    this.isLoading = true;
    this.jobPositionService.findById(id).subscribe({
      next: (job) => {
        this.isLoading = false;
        // Preenche o formulário com os dados da vaga existente
        this.jobForm.patchValue({
          ...job,
          clientId: job.client?.id ?? null
        });
        
        this.selectedTechIds = (job.technologies ?? [])
          .map(technology => technology.id)
          .filter((id): id is number => typeof id === 'number');
      },
      error: (err) => {
        this.isLoading = false;
        this.errorMessage = extractApiErrorMessage(err, 'Erro ao carregar os dados da vaga para edição.');
        console.error('Erro ao carregar vaga:', err);
      }
    });
  }

  get selectedTechnologies(): number[] {
    return this.selectedTechIds;
  }

  logout(): void {
    this.authService.logout();
  }

  onTechToggle(techId: number, event: Event): void {
    const isChecked = (event.target as HTMLInputElement).checked;
    if (isChecked) {
      this.selectedTechIds.push(techId);
    } else {
      this.selectedTechIds = this.selectedTechIds.filter(id => id !== techId);
    }
  }

  onSubmit(): void {
    if (this.jobForm.invalid) {
      this.jobForm.markAllAsTouched();
      return;
    }

    this.isLoading = true;
    this.errorMessage = '';
    this.successMessage = '';

    const payload = {
      ...this.jobForm.value,
      technologyIds: this.selectedTechIds
    };

    // 🟢 Decide se chama o create (POST) ou o update (PUT)
    const request$ = this.isEditMode && this.jobId
      ? this.jobPositionService.update(this.jobId, payload)
      : this.jobPositionService.create(payload);

    request$.subscribe({
      next: (res) => {
        this.isLoading = false;
        this.successMessage = this.isEditMode ? 'Vaga atualizada com sucesso!' : 'Vaga cadastrada com sucesso!';

        // Redireciona de volta para a listagem após 1.5 segundos ou limpa se for cadastro
        setTimeout(() => {
          this.router.navigate(['/jobs']);
        }, 1500);
      },
      error: (err) => {
        this.isLoading = false;
        this.errorMessage = extractApiErrorMessage(
          err,
          this.isEditMode
            ? 'Erro ao atualizar a vaga.'
            : 'Erro ao cadastrar a vaga.'
        );
        console.error('Erro na operação:', err);
      }
    });
  }
}