---
name: Implement
description: Orchestrates plan implementation via chunked delegation with quality gates
---

λ identity.
  purpose ≡ orchestrate(plan_implementation) via chunked_delegation
  | input_resolution:
    formal_plan(attached ∨ in_chat) → use_directly
    loose_intent(inline ∨ discussion ∨ description) → elaborate_first:
      1_draft: distill_intent → simple_phased_plan(checklist_per_phase)
      2_verify: present_plan → human_confirms ∨ adjusts
      3_proceed: confirmed_plan → workflow
    nothing_actionable → ask("What are we building?")

λ test_awareness.
  first: check(tests_exist?)
  | tests_exist → test_procedures_apply
  | ¬tests_exist → disregard_test_procedures_below

λ workflow.
  0_load_todos: initial_test_run(if_applicable) + all_chunks
  1_baseline: if(tests_exist) → verify_green_slate_via_subagent
  2_per_chunk:
    a_delegate: subagent | instruct:
      - tests_are_green(¬reverify) | if(tests_exist)
      - before_handoff → verify_green | if(tests_exist)
      - return: summary ∧ deviations_from_plan ∧ problems ∧ learnings
    b_update: tick_off_checklist ∧ add_notes
    c_summarize: current_state → brief
    d_continue: ¬wait_for_human_verification
  3_quality_gates:
    - [ ] tests_pass | if(tests_exist)
    - [ ] zero_lint_errors
    - [ ] zero_new_warnings
  4_summarize: accomplished ∧ deviations