#include "llvm/Pass.h"
#include "llvm/IR/Function.h"
#include "llvm/Support/raw_ostream.h"
#include "llvm/IR/LegacyPassManager.h"
#include "llvm/Transforms/IPO/PassManagerBuilder.h"
#include "llvm/Transforms/Utils/Local.h"
#include "llvm/IR/InstIterator.h"
#include "llvm/ADT/SmallVector.h"
#include "llvm/IR/Instruction.h"
#include "llvm/IR/InstrTypes.h"

#include <set>
#include <map>
#include <vector>

using namespace llvm;

typedef std::set<Instruction*>* InstSet;
typedef std::map<BasicBlock*, InstSet> BBMap;
typedef std::map<Instruction*, InstSet> InstMap;

namespace {

  struct MyDCE : public FunctionPass {
    static char ID;
    MyDCE() : FunctionPass(ID) {}

    virtual bool runOnFunction(Function &F) {

        // Print live set.
        InstSet deadcode = findDeadCode(F,true);

        while(deadcode->size() > 0){
            for(std::set<Instruction*>::iterator it = deadcode->begin(); it != deadcode->end(); it++) {
                (*it)->eraseFromParent();
            }
            deadcode = findDeadCode(F, false);
        }

        return true;
    }

    // Return a set of Dead Instruction. set print_liveness to be true to print the live set.
      virtual InstSet findDeadCode(Function &F,bool print_liveness){

          std::set<BasicBlock*> bb_WL;
          BBMap bb_liveIn;
          BBMap bb_liveOut;
          InstMap inst_liveIn;
          InstMap inst_liveOut;
          Function::iterator funIter;
          BasicBlock::iterator bbIter;
          std::set<Instruction*>::iterator it;


          for (Function::iterator bb = F.begin(), e = F.end(); bb != e; ++bb) {

              BasicBlock *basicBlock = (&*bb);
              bb_WL.insert(basicBlock);
              InstSet liveIn = new std::set<Instruction*>();
              bb_liveIn.insert(std::pair<BasicBlock*, InstSet>(basicBlock, liveIn));

              InstSet liveOut = new std::set<Instruction*>();
              bb_liveOut.insert(std::pair<BasicBlock*, InstSet>(basicBlock, liveOut));

          }


          while (!( bb_WL.empty() ))
          {
              BasicBlock *bb = *( bb_WL.begin() );
              bb_WL.erase(bb);

              // Calculate gen set and kill set for each block.
              InstSet gen = genSet(bb);
              InstSet kill = killSet(bb);

              // Debug ----------------------------------------------------------------
              /*
              errs() << *bb << "\n";

              errs() << "Gen size : " << gen->size() << "Kill Size : " << kill->size() << "\n";

              for (it = gen->begin(); it != gen->end(); it++) {
                  errs() << "Gen \n";
                  (*it)->printAsOperand(errs(),false);
                  errs() << "\n";
              }

              for (it = kill->begin(); it != kill->end(); it++) {
                  errs() << "Kill \n";

                  (*it)->printAsOperand(errs(),false);
                  errs() << "\n";
              }
              */

              // Calculate the In and Out set of the basic block.
              InstSet liveOut = bbLiveAfter(bb, bb_liveIn);
              delete bb_liveOut[bb];
              bb_liveOut[bb] = liveOut;

              InstSet temp_liveIn = bb_liveIn[bb];
              // In = Gen U (Out - Kill)
              InstSet liveIn = getNewIn(liveOut, gen, kill);

              // Debug ---------------------------------------------------------
              /*
              errs() << *bb << "\n";
              errs() << "liveIn size : " << liveIn->size() << "liveOut Size : " << liveOut->size() << "\n";
              for (it = liveIn->begin(); it != liveIn->end(); it++) {
                  errs() << "liveIn \n";
                  (*it)->printAsOperand(errs(),false);
                  errs() << "\n";
              }

              for (it = liveOut->begin(); it != liveOut->end(); it++) {
                  errs() << "liveOut \n";

                  (*it)->printAsOperand(errs(),false);
                  errs() << "\n";
              }

              */

              if (!compareTwoInstSets(temp_liveIn,liveIn))
              {
                  delete temp_liveIn;
                  bb_liveIn[bb] = liveIn;
                  for (pred_iterator pit = pred_begin(bb); pit != pred_end(bb); pit++) {
                      bb_WL.insert(*pit);
                  }
              }
              else
              {
                  delete liveIn;
              }

              delete gen;
              delete kill;
          }

          // Calculate live set per Instruction inside basic block.
          for (funIter = F.begin(); funIter != F.end(); funIter++)
          {
              BasicBlock *bb = &*funIter;
              bbIter = bb->end();
              bool block_ender = true;
              Instruction *this_inst, *succ_inst;

              do {
                  bbIter--;
                  succ_inst = this_inst;
                  this_inst = &*bbIter;

                  inst_liveOut[this_inst] = new std::set<Instruction*>();
                  if (block_ender) {
                      block_ender = false;
                      *(inst_liveOut[this_inst]) = *(bb_liveOut[bb]);
                  }
                  else {
                      *(inst_liveOut[this_inst]) = *(inst_liveIn[succ_inst]);
                  }

                  inst_liveIn[this_inst] = instLiveBefore(this_inst, inst_liveOut);
              } while (bbIter != bb->begin());
          }

          InstSet useless = new std::set<Instruction*>();
          for (funIter = F.begin(); funIter != F.end(); funIter++)
          {
              BasicBlock *bb = &*funIter;
              for (bbIter = bb->begin(); bbIter != bb->end(); bbIter++) {
                  Instruction *inst = &*bbIter;
                  if (canBeRemoved(inst)) {
                      std::set<Instruction*>::iterator findInst = inst_liveOut[inst]->find(inst);
                      if (findInst == inst_liveOut[inst]->end())
                          useless->insert(inst);
                  }
              }
          }

          // Print live set for each instruction need to be print.
          if(print_liveness){
              //Debug ----------------------------------------

              /*
              //errs() << "Print Liveness \n";
              for (funIter = F.begin(); funIter != F.end(); funIter++)
              {
                  BasicBlock *bb = &*funIter;
                  for (bbIter = bb->begin(); bbIter != bb->end(); bbIter++) {
                      Instruction *inst = &*bbIter;

                      printInstSet(inst_liveIn[inst]);
                      errs() << *inst << "\n";
                      printInstSet(inst_liveOut[inst]);

                  }
              }
              errs() << "====================\n";
              */


              for (funIter = F.begin(); funIter != F.end(); funIter++)
              {
                  BasicBlock *bb = &*funIter;
                  Instruction *inst;
                  bool isFirst = true;
                  for (bbIter = bb->begin(); bbIter != bb->end(); bbIter++) {
                      inst = &*bbIter;
                      if(isa<PHINode>(inst)){
                      } else{
                          if(isFirst && inst->isTerminator()){}
                          else{
                          printInstSet(inst_liveIn[inst]);}
                      }
                      isFirst = false;
                  }
                  if(inst->isTerminator() && !isa<ReturnInst>(inst)){}
                  else {
                      printInstSet(inst_liveOut[inst]);
                  }
              }
          }

          return useless;

      }


      virtual void printInstSet(InstSet instSet){

        int size = instSet->size();

        errs() << "{";
        if(size == 0){
            errs() << "}\n";
            return;
        }

        int con = 0;
        std::set<Instruction*>::iterator it;
        for (it = instSet->begin(); it != instSet->end(); it++) {
            (*it)->printAsOperand(errs(),false);
            if(++con < size){
                errs()<<",";
            }
        }

        errs() << "}\n";
        return;
    }


     virtual InstSet instLiveBefore(Instruction* inst, std::map<Instruction*, InstSet> &liveAfter) {
          InstSet liveBefore = new std::set<Instruction*>();
          *liveBefore = *(liveAfter[inst]);
          if(!inst->isTerminator()){
              liveBefore->erase(inst);
          }
          User::op_iterator opit;
          //errs() << "Instruction : " << *inst << "\n\n";
          for (opit = inst->op_begin(); opit != inst->op_end(); opit++)
          {
              Use *use = &*opit;
              Value *used_val = use->get();
              Instruction *used_inst = dyn_cast<Instruction>(used_val);
              if (used_inst != nullptr) {
                  //errs() << *used_inst << "\n";

                  liveBefore->insert(used_inst);
              }

          }

          return liveBefore;
      }

      virtual bool canBeRemoved(Instruction *inst){
        unsigned opcode = inst->getOpcode();

        if(inst->mayHaveSideEffects() || inst->isTerminator()){
            return false;
        }

          return ( inst->isBinaryOp()
                   || inst->isCast()
                   || opcode == 30
                   || opcode == 31
                   || opcode == 33
                   || opcode == 56
                   || opcode == 60
                   || opcode == 61
                   || opcode == 63
                   || opcode == 64
                   || opcode == 52
                   || opcode == 53
          );

    }


      virtual bool compareTwoInstSets(InstSet A, InstSet B)
      {
          if (A->size() != B->size())
              return false;
          else
          {
              for (auto &value : *A)
              {
                  if (B->find(value) == B->end())
                  {
                      return false;
                  }
              }
          }

          return true;
      }

      virtual InstSet genSet(BasicBlock* bb) {
          InstSet gen = new std::set<Instruction*>();
          BasicBlock::iterator bbit;

          InstSet defined = new std::set<Instruction*>();
          for (bbit = bb->begin(); bbit != bb->end(); bbit++)
          {
              Instruction *inst = &*bbit;
              User::op_iterator opit;
              for (opit = inst->op_begin(); opit != inst->op_end(); opit++)
              {
                  Use *use = &*opit;
                  Value *used_val = use->get();
                  Instruction *used_inst = dyn_cast<Instruction>(used_val);
                  if (used_inst != nullptr) {
                      if (defined->find(used_inst) == defined->end() ){
                          gen->insert(used_inst);
                      }
                  }
              }
              defined->insert(inst);
          }
          delete defined;
          return gen;
      }


      virtual InstSet killSet(BasicBlock* bb) {
          InstSet kill = new std::set<Instruction*>();
          for (BasicBlock::iterator bbit = bb->begin(); bbit != bb->end(); bbit++) {
              Instruction* inst = &*bbit;
              if(inst->isTerminator()){

              }/*else if(isa<CallInst>(inst)){
                  errs() << "Call Inst " << inst << "\n";
                  inst->printAsOperand(errs(),false);
              }*/
              else {
                  kill->insert(&*bbit);
              }
          }
          return kill;
      }

      virtual InstSet bbLiveAfter(BasicBlock* bb, std::map<BasicBlock*, InstSet> &liveBefore) {
          InstSet liveAfter = new std::set<Instruction*>();
          for (succ_iterator sit = succ_begin(bb); sit != succ_end(bb); sit++) {
              InstSet liveBefore_bb = liveBefore[*sit];
              std::set<Instruction*>::iterator it;
              for (it = liveBefore_bb->begin(); it != liveBefore_bb->end(); it++) {
                  liveAfter->insert(*it);
              }
          }
          return liveAfter;
      }


      virtual InstSet getNewIn(InstSet input, InstSet gen, InstSet kill) {
          InstSet result = new std::set<Instruction*>();
          std::set<Instruction*>::iterator it;
          for (it = input->begin(); it != input->end(); it++) {
              result->insert(*it);
          }
          for (it = kill->begin(); it != kill->end(); it++) {
              result->erase(*it);
          }
          for (it = gen->begin(); it != gen->end(); it++) {
              result->insert(*it);
          }
          return result;
      }

  };
}



char MyDCE::ID = 0;

__attribute__((unused)) static RegisterPass<MyDCE>
        X("live", "My dead code elimination"); // NOLINT