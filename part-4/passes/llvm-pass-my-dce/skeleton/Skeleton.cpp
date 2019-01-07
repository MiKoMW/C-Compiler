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

        InstSet deadcode = findDeadCode(F,true);
        while(deadcode->size() > 0){
            for(std::set<Instruction*>::iterator it = deadcode->begin(); it != deadcode->end(); it++) {
                (*it)->eraseFromParent();
            }
            deadcode = findDeadCode(F, false);
        }

        return true;
    }


      virtual InstSet findDeadCode(Function &F,bool print_liveness){

          std::set<BasicBlock*> bb_WL;
          BBMap bb_liveBefore;
          BBMap bb_liveAfter;
          InstMap inst_liveBefore;
          InstMap inst_liveAfter;
          Function::iterator funIter;
          BasicBlock::iterator bbIter;
          std::set<Instruction*>::iterator it;

          for (Function::iterator bb = F.begin(), e = F.end(); bb != e; ++bb) {

              BasicBlock *basicBlock = (&*bb);
              bb_WL.insert(basicBlock);
              InstSet liveBefore = new std::set<Instruction*>();
              bb_liveBefore.insert(std::pair<BasicBlock*, InstSet>(basicBlock, liveBefore));

              InstSet liveAfter = new std::set<Instruction*>();
              bb_liveAfter.insert(std::pair<BasicBlock*, InstSet>(basicBlock, liveAfter));

          }

          while (!( bb_WL.empty() ))
          {
              BasicBlock *bb = *( bb_WL.begin() );
              bb_WL.erase(bb);

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

              InstSet liveAfter = bbLiveAfter(bb, bb_liveBefore);
              delete bb_liveAfter[bb];
              bb_liveAfter[bb] = liveAfter;

              InstSet liveBefore = bb_liveBefore[bb];
              InstSet newLiveBefore = applyGenKill(liveAfter, gen, kill);

              // Debug ---------------------------------------------------------
              /*
              errs() << *bb << "\n";
              errs() << "newLiveBefore size : " << newLiveBefore->size() << "liveAfter Size : " << liveAfter->size() << "\n";
              for (it = newLiveBefore->begin(); it != newLiveBefore->end(); it++) {
                  errs() << "newLiveBefore \n";
                  (*it)->printAsOperand(errs(),false);
                  errs() << "\n";
              }

              for (it = liveAfter->begin(); it != liveAfter->end(); it++) {
                  errs() << "liveAfter \n";

                  (*it)->printAsOperand(errs(),false);
                  errs() << "\n";
              }

              */

              if (!compareTwoInstSets(liveBefore,newLiveBefore))
              {
                  delete liveBefore;
                  bb_liveBefore[bb] = newLiveBefore;
                  for (pred_iterator pit = pred_begin(bb); pit != pred_end(bb); pit++) {
                      bb_WL.insert(*pit);
                  }
              }
              else
              {
                  delete newLiveBefore;
              }

              delete gen;
              delete kill;
          }

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

                  inst_liveAfter[this_inst] = new std::set<Instruction*>();
                  if (block_ender) {
                      block_ender = false;
                      *(inst_liveAfter[this_inst]) = *(bb_liveAfter[bb]);
                  }
                  else {
                      *(inst_liveAfter[this_inst]) = *(inst_liveBefore[succ_inst]);
                  }

                  inst_liveBefore[this_inst] = instLiveBefore(this_inst, inst_liveAfter);
              } while (bbIter != bb->begin());
          }

          InstSet useless = new std::set<Instruction*>();
          for (funIter = F.begin(); funIter != F.end(); funIter++)
          {
              BasicBlock *bb = &*funIter;
              for (bbIter = bb->begin(); bbIter != bb->end(); bbIter++) {
                  Instruction *inst = &*bbIter;
                  if (canBeRemoved(inst)) {
                      std::set<Instruction*>::iterator findInst = inst_liveAfter[inst]->find(inst);
                      if (findInst == inst_liveAfter[inst]->end())
                          useless->insert(inst);
                  }
              }
          }

          if(print_liveness){
              //Debug ----------------------------------------

              /*
              //errs() << "Print Liveness \n";
              for (funIter = F.begin(); funIter != F.end(); funIter++)
              {
                  BasicBlock *bb = &*funIter;
                  for (bbIter = bb->begin(); bbIter != bb->end(); bbIter++) {
                      Instruction *inst = &*bbIter;

                      printInstSet(inst_liveBefore[inst]);
                      errs() << *inst << "\n";
                      printInstSet(inst_liveAfter[inst]);

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
                          printInstSet(inst_liveBefore[inst]);}
                      }
                      isFirst = false;
                  }
                  if(inst->isTerminator() && !isa<ReturnInst>(inst)){}
                  else {
                      printInstSet(inst_liveAfter[inst]);
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
                   || inst->isShift()
                   || opcode == 26 // Alloca
                   || opcode == 27 // Load
                   || opcode == 29 // GetElementPtr
                   || opcode == 49 // Select
                   || opcode == 53 // ExtractElement
                   || opcode == 56 // ExtractValue
                   || opcode == 45 // ICmp
                   || opcode == 46 // FCmp
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


      virtual InstSet applyGenKill(InstSet input, InstSet gen, InstSet kill) {
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